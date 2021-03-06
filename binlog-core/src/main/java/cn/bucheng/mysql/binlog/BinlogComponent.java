package cn.bucheng.mysql.binlog;

import cn.bucheng.mysql.aware.BeanFactoryUtils;
import cn.bucheng.mysql.callback.BinLogConfigHook;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * @author buchengyin
 * @create 2019/7/27 8:33
 * @describe
 */
@Component
@Slf4j
@Order(Integer.MAX_VALUE)
public class BinlogComponent implements CommandLineRunner {

    private BinaryLogClient client;
    @Autowired
    private BinLogConfig config;
    @Autowired
    private CompositeListener listener;


    public void init() {
        Thread thread = new Thread(() -> {
            client = new BinaryLogClient(
                    config.getHost(),
                    config.getPort(),
                    config.getUsername(),
                    config.getPassword()
            );
            tryResetConfig(config);
            if (!StringUtils.isEmpty(config.getFile())) {
                client.setBinlogFilename(config.getFile());
            }

            if (config.getPosition() != null && !config.getPosition().equals(-1L)) {
                client.setBinlogPosition(config.getPosition());
            }
            client.registerEventListener(listener);

            try {
                log.info("connecting to cn.bucheng.mysql start");
                client.connect();
                log.info("connecting to cn.bucheng.mysql done");
            } catch (IOException ex) {
                ex.printStackTrace();
                log.error(ex.toString() + ex.getMessage());
            }
        });

        thread.setName("binlog-listener-thread");
        thread.setDaemon(true);
        thread.start();
        log.info("start binlog listener client ");
    }

    /**
     * 试图重新进行设置binlog的配置
     * @param binLogConfig
     */
    private void tryResetConfig(BinLogConfig binLogConfig) {
        String[] beanNamesForType = BeanFactoryUtils.getBeanFactory().getBeanNamesForType(BinLogConfigHook.class);
        if (beanNamesForType != null && beanNamesForType.length != 0) {
            BinLogConfigHook binlogConfigMapper = BeanFactoryUtils.getBeanFactory().getBean(BinLogConfigHook.class);
            binlogConfigMapper.config(binLogConfig);
        }
    }


    @PreDestroy
    public void close() {
        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        init();
    }
}
