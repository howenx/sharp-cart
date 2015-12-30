package filters;

import domain.SysParameter;
import mapper.SkuMapper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.db.DB;
import play.db.DBApi;

import javax.sql.DataSource;
import java.io.InputStream;

/**
 *
 * Created by howen on 15/10/23.
 */
public class Global extends GlobalSettings {

    public void onStart(Application app) {}

    public void onStop(Application app) {
        Logger.error("Application shutdown...");
    }
}
