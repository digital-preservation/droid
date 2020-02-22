package uk.gov.nationalarchives.droid.results.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;

public class ResultHandlerDaoFactory implements FactoryBean<ResultHandlerDao> {

    private static final String CONSOLE = "stdout"; //TODO: what should this be?
    private static final Logger LOG = LoggerFactory.getLogger(WriterResultHandlerDao.class);

    private DataSource datasource;
    private ItemWriter itemWriter;
    private Writer writer;

    public ResultHandlerDaoFactory() {
    }

    public ResultHandlerDaoFactory(DataSource datasource) {
        this(datasource, null, (Writer) null);
    }

    public ResultHandlerDaoFactory(DataSource datasource, ItemWriter itemWriter) {
        this(datasource, itemWriter, (Writer) null);
    }

    public ResultHandlerDaoFactory(DataSource datasource, ItemWriter itemWriter, Writer writer) {
        setDatasource(datasource);
        setItemWriter(itemWriter);
        setWriter(writer);
    }

    public ResultHandlerDaoFactory(DataSource datasource, ItemWriter itemWriter, String outputFilePath) {
        setDatasource(datasource);
        setItemWriter(itemWriter);
        setOutputFilePath(outputFilePath);
    }

    public ResultHandlerDao getObject() {
        final ResultHandlerDao result;
        if (writer == null) {
            result = new JDBCBatchResultHandlerDao(datasource);
        } else {
            result = new WriterResultHandlerDao(itemWriter, writer, datasource);
        }
        result.init();
        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return ResultHandlerDao.class;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    public void setItemWriter(ItemWriter itemWriter) {
        this.itemWriter = itemWriter;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public void setOutputFilePath(String outputFilePath) {
        if (outputFilePath != null && !outputFilePath.trim().isEmpty()) {
            if (CONSOLE.equals(outputFilePath.toLowerCase())) {
                writer = new PrintWriter(System.out);
            } else {
                try {
                    writer = new PrintWriter(outputFilePath);
                } catch (FileNotFoundException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

}
