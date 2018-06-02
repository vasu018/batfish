package org.batfish.symbolic.ainterpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.SatAssignment;
import org.batfish.symbolic.utils.Tuple;

public class ReachabilityUnderDomain implements IAbstractDomain<RouteAclStateSetPair> {

  // Reference to the BDD network factory
  protected BDDNetFactory _netFactory;

  // Reference to BDD Route variables
  protected BDDRoute _variables;

  // Helper object to encapsulate common functionality
  protected DomainHelper _domainHelper;

  protected BDDNetwork _network;

  public ReachabilityUnderDomain(Graph graph, BDDNetFactory netFactory) {
    _netFactory = netFactory;
    _variables = _netFactory.routeVariables();
    _domainHelper = new DomainHelper(netFactory);
    _network = BDDNetwork.create(graph, netFactory, false);
  }

  public BDDNetFactory getNetFactory() {
    return _netFactory;
  }

  public BDDRoute getVariables() {
    return _variables;
  }

  public DomainHelper getDomainHelper() {
    return _domainHelper;
  }

  public BDDNetwork getNetwork() {
    return _network;
  }

  @Override
  public RouteAclStateSetPair bot() {
    BDD zero = _netFactory.zero();
    return new RouteAclStateSetPair(zero, zero);
  }

  @Override
  public RouteAclStateSetPair value(
      Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes) {
    BDD acc = _netFactory.zero();
    if (prefixes != null) {
      for (Prefix prefix : prefixes) {
        BDD pfx = BDDUtils.prefixToBdd(_netFactory.getFactory(), _variables, prefix);
        acc.orWith(pfx);
      }
    }
    BDD prot = _variables.getProtocolHistory().value(proto);
    BDD dst = _variables.getDstRouter().value(conf.getName());
    acc = acc.andWith(dst);
    acc = acc.andWith(prot);
    return new RouteAclStateSetPair(acc, _netFactory.zero());
  }

  @Override
  public RouteAclStateSetPair transform(RouteAclStateSetPair input, EdgeTransformer t) {
    BDDTransferFunction f = _domainHelper.lookupTransferFunction(_network, t);
    if (f != null) {
      BDD under = input.getRoutes();
      BDD allow = f.getFilter();
      BDD block = allow.not();
      BDD blockedInputs = under.and(block);
      BDD blockedPrefixes = blockedInputs.exist(_domainHelper.getAllQuantifyBits());
      BDD notBlockedPrefixes = blockedPrefixes.not();
      // Not sure why, but andWith does not work here (JavaBDD bug?)
      under = under.and(notBlockedPrefixes);
      under = _domainHelper.applyTransformerMods(under, f.getRoute(), t.getProtocol());
      BDD blocked = input.getBlockedAcls();
      BDDAcl acl = _domainHelper.lookupAcl(_network, t);
      if (acl != null) {
        BDD h = _domainHelper.headerspace(under);
        BDD toBlock = h.andWith(acl.getBdd().not());
        blocked = blocked.orWith(toBlock);
      }
      return new RouteAclStateSetPair(under, blocked);
    } else {
      return input;
    }
  }

  @Override
  public RouteAclStateSetPair merge(RouteAclStateSetPair x, RouteAclStateSetPair y) {
    BDD routes = x.getRoutes().or(y.getRoutes());
    BDD blocked = x.getBlockedAcls().or(y.getBlockedAcls());
    return new RouteAclStateSetPair(routes, blocked);
  }

  @Override
  public RouteAclStateSetPair selectBest(RouteAclStateSetPair x) {
    return x;
  }

  @Override
  public RouteAclStateSetPair aggregate(
      Configuration conf, List<AggregateTransformer> aggregates, RouteAclStateSetPair x) {
    return new RouteAclStateSetPair(
        _domainHelper.aggregates(_network, conf.getName(), x.getRoutes(), aggregates),
        x.getBlockedAcls());
  }

  @Override
  public List<Route> toRoutes(AbstractRib<RouteAclStateSetPair> value) {
    return _domainHelper.toRoutes(value.getMainRib().getRoutes());
  }

  // TODO: ensure unique reachability (i.e., no other destinations)
  @Override
  public Tuple<BDDNetFactory, BDD> toFib(Map<String, AbstractRib<RouteAclStateSetPair>> ribs) {
    Map<String, AbstractFib<RouteAclStateSetPair>> ret = new HashMap<>();
    for (Entry<String, AbstractRib<RouteAclStateSetPair>> e : ribs.entrySet()) {
      String router = e.getKey();
      AbstractRib<RouteAclStateSetPair> rib = e.getValue();
      AbstractFib<RouteAclStateSetPair> fib = new AbstractFib<>(rib, toFibSingleRouter(rib));
      ret.put(router, fib);
    }
    return new Tuple<>(_netFactory, _domainHelper.transitiveClosure(ret));
  }

  private BDD toFibSingleRouter(AbstractRib<RouteAclStateSetPair> value) {
    RouteAclStateSetPair elt = value.getMainRib();
    BDD reachablePackets = elt.getRoutes();
    reachablePackets = reachablePackets.andWith(elt.getBlockedAcls().not());
    return reachablePackets;
  }

  @Override
  public boolean reachable(
      Map<String, AbstractRib<RouteAclStateSetPair>> ribs, String src, String dst, Flow flow) {
    BDD f = BDDUtils.flowToBdd(_netFactory, flow);
    String current = src;
    while (true) {
      AbstractRib<RouteAclStateSetPair> rib = ribs.get(current);
      BDD fib = toFibSingleRouter(rib);
      BDD fibForFlow = fib.and(f);
      SatAssignment assignment = BDDUtils.satOne(_netFactory, fibForFlow);
      if (assignment == null) {
        return false;
      }
      current = assignment.getDstRouter();
      if (current.equals(dst)) {
        return true;
      }
    }
  }

  @Override
  public String debug(RouteAclStateSetPair x) {
    List<Route> ribs = _domainHelper.toRoutes(x.getRoutes());
    return ribs.toString();
  }
}
