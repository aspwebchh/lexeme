package data;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoundResult {
    public static FoundResult empty() {
        return new FoundResult(new ArrayList<>(),0);
    }

    private final List<Map<String ,Integer>> dataList;
    private final Integer dataCount;

    public FoundResult(List<Map<String,Integer>> dataList, Integer dataCount) {
        this.dataCount = dataCount;
        this.dataList = dataList;
    }

    public Map<String, Object> toData() {
        Map<String,Object> data = new HashMap<>();
        data.put("DataList", this.dataList);
        data.put("DataCount", this.dataCount);
        return data;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(toData());
    }
}
