package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundFunctionalTerm;

import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.checkNonGroundTermConstraint;

/**
 * Constraint: should contain at least one variable
 */
public class NonGroundFunctionalTermImpl extends ImmutableFunctionalTermImpl implements NonGroundFunctionalTerm {
    protected NonGroundFunctionalTermImpl(Predicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundFunctionalTermImpl(Predicate functor, ImmutableTerm... terms) {
        super(functor, ImmutableList.copyOf(terms));
        checkNonGroundTermConstraint(this);
    }

    public NonGroundFunctionalTermImpl(Function functionalTermToClone) {
        super(functionalTermToClone);
        checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }
}
