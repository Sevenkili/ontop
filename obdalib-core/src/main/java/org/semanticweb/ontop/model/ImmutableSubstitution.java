package org.semanticweb.ontop.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Declaration that the substitution is immutable.
 *
 * Implementations must not supported any mutable operation.
 *
 */
public interface ImmutableSubstitution extends Substitution {

    ImmutableMap<VariableImpl, Term> getMap();

    /**
     * Not supported (mutable operation)
     *
     * TODO: remove this operation from Substitution and from here
     */
    @Deprecated
    @Override
    boolean compose(Substitution s);


    /**
     * Not supported (mutable operation)
     *
     * TODO: remove this operation from Substitution and from here
     */
    @Deprecated
    @Override
    boolean composeTerms(Term term1, Term term2);

    /**
     * Not supported (mutable operation)
     *
     * TODO: remove this operation from Substitution and from here
     */
    @Deprecated
    @Override
    boolean composeFunctions(Function term1, Function term2);

    @Deprecated
    @Override
    ImmutableSet<VariableImpl> keySet();

}