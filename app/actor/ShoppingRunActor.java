package actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.base.Throwables;
import domain.VersionVo;
import play.Configuration;
import play.Logger;
import play.libs.F;
import play.libs.ws.WSClient;
import service.SkuService;

import javax.inject.Inject;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用于处理压缩包下载和运行脚本
 * Created by howen on 16/04/18.
 */
public class ShoppingRunActor extends AbstractActor {

    @Inject
    public ShoppingRunActor(SkuService itemService, WSClient ws, Configuration configuration) {
        receive(ReceiveBuilder.match(Long.class, message -> {
            VersionVo versionVo = new VersionVo();
            versionVo.setId(message);
            List<VersionVo> versionVoList = itemService.getVersioning(versionVo);
            if (versionVoList != null && versionVoList.size() > 0) {
                versionVo = versionVoList.get(0);
                final String fileName = versionVo.getFileName();
                final String projectName = "style-" + versionVo.getProductType();
                F.Promise<File> filePromise = ws.url(configuration.getString("zip.download.url") + URLEncoder.encode(versionVo.getDownloadLink(), "UTF-8")).get().map(response -> {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;
                    try {
                        inputStream = response.getBodyAsStream();
                        String zipPath = configuration.getString("shopping.zip.path");

                        rmShell(zipPath,projectName);

                        final File file = new File(zipPath);

                        Logger.error("shopping.zip.path文件为: " + file.getPath());

                        if (!file.exists()) {
                            if (!file.mkdirs()) Logger.error("创建文件目录出错");
                        }

                        outputStream = new FileOutputStream(new File(zipPath + fileName));

                        int read = 0;
                        byte[] buffer = new byte[1024];

                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }

                        callShell(zipPath, fileName, projectName);

                        return file;
                    } catch (IOException e) {
                        Logger.error(Throwables.getStackTraceAsString(e));
                        e.printStackTrace();
                        return null;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                });
                filePromise.get(3, TimeUnit.MINUTES);
            }

        }).matchAny(s -> {
            Logger.error("AdminRunActor received messages not matched: {}", s.toString());
            unhandled(s);
        }).build());
    }

    private void rmShell(String dist, String projectName){
        List<String> commands = Arrays.asList("bash", "-c", "sudo rm -rf " + projectName +"*");

        String output = null;
        try {
            output = exec(dist, null, commands);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(e));
        }
        Logger.error("删除---->\n" + output);
    }

    private void callShell(String dist, String fileName, String projectName) {

        List<String> commands = Arrays.asList("bash", "-c", "ls -al ; unzip " + fileName);
        String projectDir = dist + fileName.replaceAll(".zip", "");
        Logger.error("项目目录-->" + projectDir);
        List<String> commands2 = Arrays.asList("bash", "-c", "sh run.sh " + projectDir + " " + projectName);

        String output = null;
        String output2 = null;
        try {
            output = exec(dist, null, commands);
            output2 = exec(dist, null, commands2);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(e));
        }
        Logger.error("压缩---->\n" + output);
        Logger.error("执行脚本---->\n" + output2);
    }

    private String exec(String dist, String command, List<String> commands) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        ProcessBuilder pb;
        if (command != null) {
            pb = new ProcessBuilder(command);

        } else {
            pb = new ProcessBuilder(commands);
        }

        pb.directory(new File(dist));
        Process p = pb.start();
        p.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            Logger.error("failed to execute:" + p.exitValue());

            return output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(e));
            return null;
        } finally {
            reader.close();
        }
    }

}
