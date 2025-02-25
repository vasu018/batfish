package org.batfish.minesweeper.bdd;

import net.sf.javabdd.BDD;
import org.batfish.minesweeper.utils.Tuple;

/**
 * The data produced by the symbolic route policy analysis performed in {@link TransferBDD}. It is a
 * triple representing the analysis results along a particular execution path through the route
 * policy:
 *
 * <p>1. A {@link BDDRoute} that represents a function from an input announcement to the
 * corresponding output announcement produced by the analyzed route policy on this particular path.
 * Note that even if the path ends up rejecting the given route, we still record all route updates
 * that occur. We also record whether the path encountered a statement that the {@link TransferBDD}
 * analysis does not currently support, which indicates that the analysis results may not be
 * accurate.
 *
 * <p>2. A {@link BDD} that represents the set of input announcements that take this particular
 * path.
 *
 * <p>3. A boolean indicating whether the path accepts or rejects the input announcement.
 */
public class TransferReturn extends Tuple<BDDRoute, BDD> {

  private final boolean _accepted;

  TransferReturn(BDDRoute r, BDD b, boolean accepted) {
    super(r, b);
    _accepted = accepted;
  }

  TransferReturn(BDDRoute r, BDD b) {
    this(r, b, false);
  }

  public boolean getAccepted() {
    return _accepted;
  }

  public TransferReturn setAccepted(boolean accepted) {
    return new TransferReturn(getFirst(), getSecond(), accepted);
  }

  public String debug() {
    return getFirst().dot(getSecond());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TransferReturn)) {
      return false;
    }
    return super.equals(o) && _accepted == ((TransferReturn) o)._accepted;
  }

  @Override
  public int hashCode() {
    return super.hashCode() * Boolean.valueOf(_accepted).hashCode();
  }

  @Override
  public String toString() {
    return "<" + getFirst() + "," + getSecond() + "," + _accepted + ">";
  }
}
