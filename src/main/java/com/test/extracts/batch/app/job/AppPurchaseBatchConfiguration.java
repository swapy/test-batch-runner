package com.test.extracts.batch.app.job;

import com.test.extracts.batch.app.listener.TestBatchJobListener;
import com.test.extracts.batch.app.listener.TestBatchStepListener;
import com.test.extracts.batch.app.model.Purchase;
import com.test.extracts.config.datasource.BatchDatasourceConfiguration;
import com.test.extracts.config.properties.BatchConfiguration;
import com.test.extracts.config.properties.QueryConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Configuration
@Import(value = BatchDatasourceConfiguration.class)
@EnableBatchProcessing(dataSourceRef = "batchDataSource", transactionManagerRef = "batchTransactionManager")
public class AppPurchaseBatchConfiguration implements TestBatchJobListener, TestBatchStepListener {

    protected Path path;
    protected WritableResource exportFileResource;
    protected BatchConfiguration batchConfiguration;

    @PostConstruct
    void init() {
        String file = batchConfiguration.getOutputDir() + File.separator + this.batchConfiguration.getFileNamePrefix() + File.separator + this.batchConfiguration.getFileNamePrefix();
        path = Path.of(file);
        if (!path.toFile().exists()) {
            path.toFile().getParentFile().mkdirs();
        }
        exportFileResource = new FileSystemResource(path);
    }

    public String[] getNames() {
        return this.batchConfiguration.getQueryConfig().getFields();
    }

    public String getCsvColumNames() {
        String[] names = getNames();
        return String.join(",", names);
    }

    @Autowired
    public AppPurchaseBatchConfiguration(@Qualifier("appPurchaseConfig") BatchConfiguration batchConfiguration) {
        this.batchConfiguration = batchConfiguration;
    }

    @Bean
    public Job appPurchaseJob(@Qualifier("appDatasource") DataSource dataSource, @Qualifier("appTransactionManager") JdbcTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("appPurchaseJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .start(appPurchaseStep(dataSource, transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step appPurchaseStep(@Qualifier("appDatasource") DataSource dataSource, @Qualifier("appTransactionManager") JdbcTransactionManager transactionManager, JobRepository jobRepository) {
        log.info("in step appPurchaseStep");
        return new StepBuilder("in app step", jobRepository)
                .<Purchase, Purchase>chunk(batchConfiguration.getChunkSize(), transactionManager)
                .listener(chunkListener())
                .allowStartIfComplete(true)
                .reader(appPurchaseItemReader(appPurchaseQueryProvider(dataSource), dataSource))
                .processor(appPurchaseProcessor())
                .writer(appPurchaseWriter())
                .build();
    }

    @Bean(name = "appPurchaseProcessor")
    public ItemProcessor<Purchase, Purchase> appPurchaseProcessor() {
        return audit -> {
            return audit;
        };
    }

    @Bean(name = "appPurchaseWriter")
    public ItemWriter<Purchase> appPurchaseWriter() {

        DelimitedLineAggregator<Purchase> delimitedLineAggregator = new DelimitedLineAggregator<>();
        delimitedLineAggregator.setDelimiter(batchConfiguration.getDelimiter());
        delimitedLineAggregator.setFieldExtractor(appPurchaseFieldExtractor());

        FlatFileItemWriter<Purchase> itemWriter = new FlatFileItemWriterBuilder<Purchase>()
                .headerCallback(writer -> writer.write(getCsvColumNames()))
                .encoding(StandardCharsets.UTF_8.toString())
                .lineAggregator(delimitedLineAggregator)
                .shouldDeleteIfExists(true)
                .shouldDeleteIfEmpty(true)
                .append(true)
                .name("FlatFileItemWriter")
                .build();


        return new MultiResourceItemWriterBuilder<Purchase>()
                .name("file")
                .delegate(itemWriter)
                .itemCountLimitPerResource(batchConfiguration.getItemCountLimitPerResource())
                .saveState(true)
                .resourceSuffixCreator(index -> "-" + index + ".csv")
                .resource(exportFileResource)
                .build();
    }

    @Bean
    public ItemReader<Purchase> appPurchaseItemReader(PagingQueryProvider queryProvider, @Qualifier("appDatasource") DataSource appDatasource) {
        return new JdbcPagingItemReaderBuilder<Purchase>()
                .name("pagingItemReaderAppPurchase")
                .dataSource(appDatasource)
                .pageSize(batchConfiguration.getPageSize())
                .maxItemCount(batchConfiguration.getMaxItemCount())
                .queryProvider(queryProvider)
                .rowMapper(new BeanPropertyRowMapper<>(Purchase.class))
                .build();
    }

    @Bean
    public PagingQueryProvider appPurchaseQueryProvider(@Qualifier("appDatasource") DataSource dataSource) {
        QueryConfig queryConfig = batchConfiguration.getQueryConfig();
        SqlPagingQueryProviderFactoryBean provider =
                new SqlPagingQueryProviderFactoryBean();
        provider.setSelectClause(queryConfig.getSelectClause());
        provider.setFromClause(queryConfig.getFromClause());
        provider.setDataSource(dataSource);
        provider.setSortKeys(Map.of(queryConfig.getSortKey(), Order.ASCENDING));
        try {
            return provider.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FieldExtractor<Purchase> appPurchaseFieldExtractor() {
        BeanWrapperFieldExtractor<Purchase> extractor =
                new BeanWrapperFieldExtractor<>();
        extractor.setNames(getNames());
        return extractor;
    }
}
