package modules;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import mapper.IdMapper;
import mapper.SkuMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.session.SqlSessionManagerProvider;
import play.db.DBApi;
import service.IdService;
import service.IdServiceImpl;
import service.SkuService;
import service.SkuServiceImpl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.sql.DataSource;

/**
 *
 * Created by howen on 15/10/28.
 */
public class IdDBModule extends PrivateModule{

    @Override
    protected void configure() {

        install(new org.mybatis.guice.MyBatisModule() {
            @Override
            protected void initialize() {
                environmentId("id");
                //开启驼峰自动映射
                mapUnderscoreToCamelCase(true);

                bindDataSourceProviderType(DevDataSourceProvider.class);
                bindTransactionFactoryType(JdbcTransactionFactory.class);
                addMapperClass(IdMapper.class);
            }
        });

        /**
         * bind SQLsession to isolate the multiple datasources.
         */
        bind(SqlSession.class).annotatedWith(Names.named("id")).toProvider(SqlSessionManagerProvider.class).in(Scopes.SINGLETON);
        expose(SqlSession.class).annotatedWith(Names.named("id"));

        /**
         * bind service for controller or other service inject.
         */
        bind(IdService.class).to(IdServiceImpl.class);
        expose(IdService.class);

    }

    @Singleton
    private static class DevDataSourceProvider implements Provider<DataSource> {

        private final DBApi db;

        @Inject
        public DevDataSourceProvider(final DBApi db) {
            this.db = db;
        }

        @Override
        public DataSource get() {
            return db.getDatabase("id").getDataSource();
        }
    }

}
