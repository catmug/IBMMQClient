package ee.catmug;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SampleEnvSetter {

    private static final Logger logger = Logger.getLogger("com.ibm.mq.samples.jms");
    private JSONArray mqEndPoints;

    private static final String CCDT = "MQCCDTURL";
    private static final String FILEPREFIX = "file://";

    public SampleEnvSetter() {
        JSONObject mqEnvSettings = null;

        mqEndPoints = null;

        try {
            JSONParser parser = new JSONParser();
            Object data = parser.parse(new FileReader("./env.json"));
            logger.info("File read");

            mqEnvSettings = (JSONObject) data;

            if (mqEnvSettings != null) {
                logger.info("JSON Data Found");
                mqEndPoints = (JSONArray) mqEnvSettings.get("MQ_ENDPOINTS");
            }

            if (mqEndPoints == null || mqEndPoints.isEmpty()) {
                logger.warning("No Endpoints found in .json file next instruction " +
                        "will raise a null pointer exception");
            } else {
                logger.info("There is at least one MQ endpoint in the .json file");
            }

        } catch (FileNotFoundException e) {
            logger.warning(e.getMessage());
        } catch (IOException e) {
            logger.warning(e.getMessage());
        } catch (ParseException e) {
            logger.warning(e.getMessage());
        }
    }

    public String getEnvValue(String key, int index) {
        JSONObject mqAppEnv = null;
        String value = System.getenv(key);

        if ((value == null || value.isEmpty()) &&
                mqEndPoints != null &&
                ! mqEndPoints.isEmpty()) {
            mqAppEnv = (JSONObject) mqEndPoints.get(index);
            value = (String) mqAppEnv.get(key);
        }

        if (! key.contains("PASSWORD")) {
            logger.info("returning " + value + " for key " + key);
        }
        return value;
    }

    public String getCheckForCCDT() {
        String value = System.getenv(CCDT);

        if (value != null && ! value.isEmpty()) {
            String ccdtFile = value;
            if (ccdtFile.startsWith(FILEPREFIX)) {
                ccdtFile = ccdtFile.split(FILEPREFIX)[1];
                logger.info("Checking for existance of file " + ccdtFile);

                File tmp = new File(ccdtFile);
                if (! tmp.exists()) {
                    value = null;
                }

            }
        }
        return value;
    }

    public String getConnectionString() {
        List<String> coll = new ArrayList<String>();

        for (Object o : mqEndPoints) {
            JSONObject jo = (JSONObject) o;
            String s = (String) jo.get("HOST") + "(" + (String) jo.get("PORT") + ")";
            coll.add(s);
        }

        String connString = String.join(",", coll);
        logger.info("Connection string will be " + connString);

        return connString;
    }

    public int getCount() {
        return mqEndPoints.size();
    }
}