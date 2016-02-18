package modules;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import mapper.PinSkuMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.session.SqlSessionManagerProvider;
import play.db.DBApi;
import service.PromotionService;
import service.PromotionServiceImpl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.sql.DataSource;

/**
 *
 * Created by howen on 15/10/28.
 */
public class PromotionDBModule extends PrivateModule{

    @Override
    protected void configure() {

        install(new org.mybatis.guice.MyBatisModule() {
            @Override
            protected void initialize() {
                environmentId("promotion");
                //开启驼峰自动映射
                mapUnderscoreToCamelCase(true);

                bindDataSourceProviderType(DevDataSourceProvider.class);
                bindTransactionFactoryType(JdbcTransactionFactory.class);
                addMapperClass(PinSkuMapper.class);
            }
        });

        /**
         * bind SQLsession to isolate the multiple datasources.
         */
        bind(SqlSession.class).annotatedWith(Names.named("promotion")).toProvider(SqlSessionManagerProvider.class).in(Scopes.SINGLETON);
        expose(SqlSession.class).annotatedWith(Names.named("promotion"));

        /**
         * bind service for controller or other service inject.
         */
        bind(PromotionService.class).to(PromotionServiceImpl.class);
        expose(PromotionService.class);
    }

    @Singleton
    public static class DevDataSourceProvider implements Provider<DataSource> {

        private final DBApi db;

        @Inject
        public DevDataSourceProvider(final DBApi db) {
            this.db = db;
        }

        @Override
        public DataSource get() {
            return db.getDatabase("promotion").getDataSource();
        }
    }

}
