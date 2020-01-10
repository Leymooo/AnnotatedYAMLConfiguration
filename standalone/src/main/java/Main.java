import ru.leymooo.annotatedyaml.ConfigOptions;
import ru.leymooo.annotatedyaml.Configuration;
import ru.leymooo.annotatedyaml.provider.StandaloneConfigurationProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static {
    }

    public static void main(String[] args) {
        TestCfg testCfg = new TestCfg();
        testCfg.setConfigurationProvider(new StandaloneConfigurationProvider(new File("test.yml")));
        testCfg.load();
        testCfg.save();
        System.out.println(testCfg);
    }


    @ConfigOptions.Comment("TEST2")
    public static class TestCfg extends Configuration {
        @ConfigOptions.Comment("TEST")
        @ConfigOptions.ConfigKey("11-test")
        private String test = "test0";
        private TestCfg2 test2 = new TestCfg2();


        @Override
        public String toString() {
            return "TestCfg{" +
                    "test='" + test + '\'' +
                    ", test2=" + test2 +
                    '}';
        }
    }

    @ConfigOptions.Comment("TEST1")
    public static class TestCfg2 extends Configuration {
        private final Map<String, Object> testMap = new HashMap<>();
        @ConfigOptions.Comment("TEST")
        @ConfigOptions.ConfigKey("11-test")
        private String test = "test1";

        public TestCfg2() {
            testMap.put("test1", test);
            testMap.put("ggg", 5);
            testMap.put("666", 9.9);
        }

        @Override
        public String toString() {
            return "TestCfg2{" +
                    "testMap=" + testMap +
                    ", test='" + test + '\'' +
                    '}';
        }
    }
}
