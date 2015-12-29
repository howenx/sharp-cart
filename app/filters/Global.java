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

    //行邮税收税标准
    public static String POSTAL_STANDARD = "";

    public static String POSTAL_LIMIT = "";

    public static String FREE_SHIP = "";

//    Statement statement = DB.getConnection("style").createStatement();
//
//    ResultSet resultSet =statement.executeQuery("select * from sys_parameter t where 1=1 and t.parameter_code='POSTAL_STANDARD'");
//
//    while(resultSet.next()){
//        //Retrieve by column name
//        String parameter_val  = resultSet.getString("parameter_val");
//        Logger.error("对不对::: "+parameter_val);
//    }
//    resultSet.close();
//    statement.close();

//    private final DBApi db;
//    public Global(final DBApi db) {
//        this.db = db;
//    }


    public void onStart(Application app) {
//        try {
//            DataSource dataSource = DB.getDataSource("style");
//            Logger.error("草泥马: "+DB.getConnection("style").createStatement().execute(""));
//            TransactionFactory transactionFactory = new JdbcTransactionFactory();
//            Environment environment = new Environment("developmentGlobal", transactionFactory, dataSource);
//            Configuration configuration = new Configuration(environment);
//            Logger.error("草泥马: "+configuration.getDatabaseId());
//            configuration.addMapper(SkuMapper.class);
//            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
//            SqlSession sqlSession = sqlSessionFactory.openSession();
//            SkuMapper mapper = sqlSession.getMapper(SkuMapper.class);
//            SysParameter user = mapper.getSysParameter(new SysParameter(null, null, null, "POSTAL_STANDARD"));
//            Logger.error("中午和 i你们:  "+user.toString());
//            POSTAL_STANDARD = user.getParameterVal();
//            sqlSession.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void onStop(Application app) {
        Logger.error("Application shutdown...");
    }
}
