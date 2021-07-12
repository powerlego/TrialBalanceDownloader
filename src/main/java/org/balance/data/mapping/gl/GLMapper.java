package org.balance.data.mapping.gl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.data.mapping.Mapper;
import org.balance.extractor.processes.Extractor.Task;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nicholas Curl
 */
public class GLMapper extends Mapper<Map<String, List<List<Object>>>> {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(GLMapper.class);

    private final Task<?> task;
    private final List<List<Object>> data;
    private final List<String> deptCodes;
    private final List<String> header;

    public GLMapper( List<String> header,List<List<Object>> data, List<String> deptCodes,Task<?> task){
        this.task = task;
        this.data = data;
        this.header = header;
        this.deptCodes = deptCodes;
    }

    @Override
    public Map<String, List<List<Object>>> map() {
        int progress = 0;
        HashMap<String, List<List<Object>>> map = new HashMap<>();
        task.getProgressContainer().getStatus().setText("Mapping");
        task.getProgressContainer().getProgressBar().setMaximum(data.size());
        task.getProgressContainer().getProgressBar().setValue(0);
        List<Object> mapHeader = initializeHeader(header);
        for (String deptCode : deptCodes) {
            if (task.isCancelled()) {
                return new HashMap<>();
            }
            map.put(deptCode, new ArrayList<>() {{
                add(mapHeader);
            }});
        }
        map.put("", new ArrayList<>() {{
            add(mapHeader);
        }});
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        for (List<Object> datum : data) {
            if (task.isCancelled()) {
                return new HashMap<>();
            }
            List<Object> row = new ArrayList<>();
            for (int i = 0; i < mapHeader.size(); i++) {
                if (task.isCancelled()) {
                    return new HashMap<>();
                }
                switch (i) {
                    case 0:
                        if (datum.get(0) instanceof Date) {
                            Date date = (Date) datum.get(0);
                            row.add(i, format.format(date));
                        }
                        else {
                            row.add(i, datum.get(0));
                        }
                        break;
                    case 1:
                        row.add(i, datum.get(1));
                        break;
                    case 2:
                        String accountName = datum.get(2) + " " + datum.get(7);
                        row.add(i, accountName);
                        break;
                    case 3:
                        row.add(i, datum.get(4));
                        break;
                    case 4:
                        row.add(i, datum.get(8));
                        break;
                    case 5:
                        row.add(i, datum.get(9));
                        break;
                    case 6:
                        row.add(i, datum.get(10));
                        break;
                    case 7:
                        row.add(i, datum.get(11));
                        break;
                    default:
                        row.add(i, "");
                        break;
                }
            }
            Pattern pattern = Pattern.compile("^[123]\\d*");
            Matcher matcher = pattern.matcher((String) datum.get(2));
            if (matcher.find()) {
                if (((String) datum.get(3)).isBlank()) {
                    map.get("0").add(row);
                }
                else {
                    map.get((String) datum.get(3)).add(row);
                }
            }
            else {
                map.get((String) datum.get(3)).add(row);
            }
            task.getProgressContainer().getProgressBar().setValue(progress++);
        }
        return map;
    }

    @NotNull
    private static List<Object> initializeHeader(List<String> header) {
        return new ArrayList<>() {{
            add(header.get(0));
            add(header.get(1));
            add("G/L Account");
            add(header.get(4));
            add(header.get(8));
            add(header.get(9));
            add(header.get(10));
            add(header.get(11));
        }};
    }
}
