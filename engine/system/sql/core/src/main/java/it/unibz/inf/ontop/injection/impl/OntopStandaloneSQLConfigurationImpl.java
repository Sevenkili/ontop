package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.iq.executor.ProposalExecutor;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSystemSQLConfigurationImpl.OntopSystemSQLOptions;
import it.unibz.inf.ontop.injection.impl.OntopTranslationConfigurationImpl.DefaultOntopTranslationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopTranslationSQLConfigurationImpl.DefaultOntopTranslationSQLBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopTranslationSQLConfigurationImpl.OntopTranslationSQLOptions;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;


public class OntopStandaloneSQLConfigurationImpl extends OntopMappingSQLAllConfigurationImpl
        implements OntopStandaloneSQLConfiguration {

    private final OntopStandaloneSQLSettings settings;
    private final OntopSystemSQLConfigurationImpl systemConfiguration;

    OntopStandaloneSQLConfigurationImpl(OntopStandaloneSQLSettings settings, OntopStandaloneSQLOptions options) {
        super(settings, options.mappingOptions);
        this.settings = settings;
        systemConfiguration = new OntopSystemSQLConfigurationImpl(settings, options.systemOptions);
    }

    @Override
    public OntopStandaloneSQLSettings getSettings() {
        return settings;
    }

    @Override
    public Optional<IRIDictionary> getIRIDictionary() {
        return systemConfiguration.getIRIDictionary();
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                systemConfiguration.buildGuiceModules());
    }

    /**
     * Can be overloaded by sub-classes
     */
    @Override
    protected ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends ProposalExecutor>>
    generateOptimizationConfigurationMap() {
        return Stream.concat(
                    super.generateOptimizationConfigurationMap().entrySet().stream(),
                    systemConfiguration.generateOptimizationConfigurationMap().entrySet().stream())
                .distinct()
                .collect(ImmutableCollectors.toMap());
    }


    static class OntopStandaloneSQLOptions {
        final OntopSystemSQLOptions systemOptions;
        final OntopMappingSQLAllOptions mappingOptions;

        OntopStandaloneSQLOptions(OntopSystemSQLOptions systemOptions, OntopMappingSQLAllOptions mappingOptions) {
            this.systemOptions = systemOptions;
            this.mappingOptions = mappingOptions;
        }
    }



    static abstract class OntopStandaloneSQLBuilderMixin<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopMappingSQLAllBuilderMixin<B>
            implements OntopStandaloneSQLConfiguration.Builder<B> {

        private final DefaultOntopTranslationSQLBuilderFragment<B> sqlTranslationFragmentBuilder;
        private final DefaultOntopTranslationBuilderFragment<B> translationFragmentBuilder;
        private final DefaultOntopSystemBuilderFragment<B> systemFragmentBuilder;

        OntopStandaloneSQLBuilderMixin() {
            B builder = (B) this;
            this.sqlTranslationFragmentBuilder = new DefaultOntopTranslationSQLBuilderFragment<>(builder);
            this.translationFragmentBuilder = new DefaultOntopTranslationBuilderFragment<>(builder);
            this.systemFragmentBuilder = new DefaultOntopSystemBuilderFragment<>(builder);
        }

        @Override
        public B enableIRISafeEncoding(boolean enable) {
            return translationFragmentBuilder.enableIRISafeEncoding(enable);
        }

        @Override
        public B enableExistentialReasoning(boolean enable) {
            return translationFragmentBuilder.enableExistentialReasoning(enable);
        }

        @Override
        public B iriDictionary(@Nonnull IRIDictionary iriDictionary) {
            return translationFragmentBuilder.iriDictionary(iriDictionary);
        }

        @Override
        public B keepPermanentDBConnection(boolean keep) {
            return systemFragmentBuilder.keepPermanentDBConnection(keep);
        }

        @Override
        protected Properties generateProperties() {
            Properties p = super.generateProperties();
            p.putAll(systemFragmentBuilder.generateProperties());
            p.putAll(sqlTranslationFragmentBuilder.generateProperties());
            p.putAll(translationFragmentBuilder.generateProperties());
            return p;
        }

        final OntopStandaloneSQLOptions generateStandaloneSQLOptions() {
            OntopMappingSQLAllOptions sqlMappingOptions = generateMappingSQLAllOptions();
            OntopTranslationConfigurationImpl.OntopTranslationOptions translationOptions =
                    this.translationFragmentBuilder.generateTranslationOptions(
                        sqlMappingOptions.mappingSQLOptions.mappingOptions.obdaOptions,
                        sqlMappingOptions.mappingSQLOptions.mappingOptions.optimizationOptions);

            OntopTranslationSQLOptions sqlTranslationOptions = sqlTranslationFragmentBuilder.generateSQLTranslationOptions(
                    translationOptions,
                    sqlMappingOptions.mappingSQLOptions.sqlOptions);

            OntopSystemSQLOptions systemSQLOptions = new OntopSystemSQLOptions(sqlTranslationOptions);

            return new OntopStandaloneSQLOptions(systemSQLOptions, sqlMappingOptions);
        }

    }

    public static final class BuilderImpl<B extends OntopStandaloneSQLConfiguration.Builder<B>>
            extends OntopStandaloneSQLBuilderMixin<B> {

        @Override
        public OntopStandaloneSQLConfiguration build() {
            OntopStandaloneSQLSettings settings = new OntopStandaloneSQLSettingsImpl(generateProperties(),
                    isR2rml());
            OntopStandaloneSQLOptions options = generateStandaloneSQLOptions();
            return new OntopStandaloneSQLConfigurationImpl(settings, options);
        }
    }



}
