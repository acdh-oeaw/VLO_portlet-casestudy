package eu.clarin.cmdi.vlo.importer;

import com.google.common.collect.ImmutableMap;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryNamePostProcessor extends AbstractPostProcessor {

    private final static Logger LOG = LoggerFactory.getLogger(CountryNamePostProcessor.class);
    private final String countryComponentUrl;
    private Map<String, String> countryCodeMap;

    public CountryNamePostProcessor(VloConfig config) {
        super(config);
        countryComponentUrl = getConfig().getCountryComponentUrl();
    }

    /**
     * Returns the country name based on the mapping defined in the CMDI
     * component:
     * http://catalog.clarin.eu/ds/ComponentRegistry/?item=clarin.eu:cr1:c_1271859438104
     * If no mapping is found the original value is returned.
     *
     * @param value extracted "country" value from CMDI file
     * @return List of country names
     */
    @Override
    public List<String> process(String value, CMDIData cmdiData) {
        if (value == null) {
            return Collections.singletonList(null);
        } else {
            final String normalized = getCountryCodeMap().getOrDefault(value.toUpperCase(), value);
            return Collections.singletonList(normalized);
        }
    }

    private synchronized Map<String, String> getCountryCodeMap() {
        if (countryCodeMap == null) {
            countryCodeMap = createCountryCodeMap();
        }
        return countryCodeMap;
    }

    private Map<String, String> createCountryCodeMap() {
        LOG.info("Creating country code map from {}", countryComponentUrl);
        try {
            return ImmutableMap.copyOf(CommonUtils.createCMDIComponentItemMap(countryComponentUrl));
        } catch (VTDException | IOException e) {
            if (CommonUtils.shouldSwallowLookupErrors()) {
                return Collections.emptyMap();
            } else {
                throw new RuntimeException("Cannot instantiate postProcessor:", e);
            }
        }
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }

}
