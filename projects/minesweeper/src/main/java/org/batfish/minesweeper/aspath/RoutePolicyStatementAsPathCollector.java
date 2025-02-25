package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.BufferedStatement;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.batfish.datamodel.routing_policy.statement.ExcludeAsPath;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.RemoveTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.ReplaceAsesInAsSequence;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetDefaultTag;
import org.batfish.datamodel.routing_policy.statement.SetEigrpMetric;
import org.batfish.datamodel.routing_policy.statement.SetIsisLevel;
import org.batfish.datamodel.routing_policy.statement.SetIsisMetricType;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetVarMetricType;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.StatementVisitor;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all AS-path regexes in a route-policy {@link Statement}. */
@ParametersAreNonnullByDefault
public class RoutePolicyStatementAsPathCollector
    implements StatementVisitor<Set<SymbolicAsPathRegex>, Tuple<Set<String>, Configuration>> {
  @Override
  public Set<SymbolicAsPathRegex> visitBufferedStatement(
      BufferedStatement bufferedStatement, Tuple<Set<String>, Configuration> arg) {
    return bufferedStatement.getStatement().accept(this, arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitCallStatement(
      CallStatement callStatement, Tuple<Set<String>, Configuration> arg) {
    if (arg.getFirst().contains(callStatement.getCalledPolicyName())) {
      // If we have already visited this policy then don't visit again
      return ImmutableSet.of();
    }
    // Otherwise update the set of seen policies and recurse.
    arg.getFirst().add(callStatement.getCalledPolicyName());

    return visitAll(
        arg.getSecond()
            .getRoutingPolicies()
            .get(callStatement.getCalledPolicyName())
            .getStatements(),
        arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitComment(
      Comment comment, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitIf(If if1, Tuple<Set<String>, Configuration> arg) {
    ImmutableSet.Builder<SymbolicAsPathRegex> builder = ImmutableSet.builder();
    return builder
        .addAll(if1.getGuard().accept(new BooleanExprAsPathCollector(), arg))
        .addAll(visitAll(if1.getTrueStatements(), arg))
        .addAll(visitAll(if1.getFalseStatements(), arg))
        .build();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitPrependAsPath(
      PrependAsPath prependAsPath, Tuple<Set<String>, Configuration> arg) {
    // if/when we update TransferBDD to support AS-path prepending, we will need to update this as
    // well
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitReplaceAsesInAsSequence(
      ReplaceAsesInAsSequence replaceAsesInAsPathSequence) {
    // if/when we update TransferBDD to support AS-path replacing, we will need to update this as
    // well
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitExcludeAsPath(
      ExcludeAsPath excludeAsPath, Tuple<Set<String>, Configuration> arg) {
    // if/when TransferBDD gets updated to support AS-path excluding, this will have to be updated
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitRemoveTunnelEncapsulationAttribute(
      RemoveTunnelEncapsulationAttribute removeTunnelAttribute,
      Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetAdministrativeCost(
      SetAdministrativeCost setAdministrativeCost, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetCommunities(
      SetCommunities setCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetDefaultPolicy(
      SetDefaultPolicy setDefaultPolicy, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetEigrpMetric(
      SetEigrpMetric setEigrpMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetIsisLevel(
      SetIsisLevel setIsisLevel, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetIsisMetricType(
      SetIsisMetricType setIsisMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetLocalPreference(
      SetLocalPreference setLocalPreference, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetMetric(
      SetMetric setMetric, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetNextHop(
      SetNextHop setNextHop, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetOrigin(
      SetOrigin setOrigin, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetOspfMetricType(
      SetOspfMetricType setOspfMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetTag(
      SetTag setTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetDefaultTag(
      SetDefaultTag setDefaultTag, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetTunnelEncapsulationAttribute(
      SetTunnelEncapsulationAttribute setTunnelAttribute, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetVarMetricType(
      SetVarMetricType setVarMetricType, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitSetWeight(
      SetWeight setWeight, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitStaticStatement(
      StaticStatement staticStatement, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitTraceableStatement(
      TraceableStatement traceableStatement, Tuple<Set<String>, Configuration> arg) {
    return visitAll(traceableStatement.getInnerStatements(), arg);
  }

  public Set<SymbolicAsPathRegex> visitAll(
      List<Statement> statements, Tuple<Set<String>, Configuration> arg) {
    return statements.stream()
        .flatMap(stmt -> stmt.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
