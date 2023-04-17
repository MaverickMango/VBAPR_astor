package fr.inria.astor.test.repair.approaches.VBAPR.patch;

import com.google.gson.*;
import fr.inria.astor.util.FileTools;
import fr.inria.main.test.AstorPatchInfo;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class PatchStatTest {
    @Test
    public void testPatchesNums() {
        String buggyBase = "/mnt/workspace/";
        String repairBase = "VBAPRResult/";//_exhausted_Edit4Exp_compiled
        String proj = "Math";
        String id = "33";
        String output_file = buggyBase + repairBase + proj + "/VBAPRMain-" + proj + "_" + id + "/astor_output.json";
        JsonElement jsonElement = new JsonParser().parse(FileTools.readFileByLines(output_file));
        System.out.println(proj + "_" + id);
        outputPatchStats(jsonElement);
    }

    void getPatchStats(JsonArray patches, Map<String, Set<String>> oriFinalMap, List<String> samePatchBugs, List<String> duplicatePatch) {
        for (int i = 0; i < patches.size(); i++) {
            JsonObject patch = patches.get(i).getAsJsonObject();
            JsonArray patchhunks = patch.getAsJsonArray("patchhunks");
            assert patchhunks.size() > 0;
            JsonObject first = patchhunks.get(0).getAsJsonObject();
            JsonPrimitive original_code = first.getAsJsonPrimitive("ORIGINAL_CODE");
            JsonPrimitive patchhunk_code = first.getAsJsonPrimitive("PATCH_HUNK_CODE");
            if (patchhunks.size() > 1) {
                patchhunk_code = ((JsonObject) patchhunks.get(patchhunks.size() - 1)).getAsJsonPrimitive("PATCH_HUNK_CODE");
            }
            JsonPrimitive patchID = patch.getAsJsonPrimitive("VARIANT_ID");

            String key = getKey(original_code, first.getAsJsonPrimitive("OPERATOR"), first.getAsJsonPrimitive("LINE"));
            String patchStr = patchhunk_code == null ? "" : patchhunk_code.getAsString();

            if (original_code.getAsString().equals(patchStr)) {
                samePatchBugs.add(patchID.getAsString());
            }
            if (!oriFinalMap.containsKey(key)) {
                oriFinalMap.put(key, new HashSet<>());
            } else {
                Set<String> patchStr_same = oriFinalMap.get(key);
                if (patchStr_same.contains(patchStr))
                    duplicatePatch.add(patchID.getAsString());
            }
            oriFinalMap.get(key).add(patchStr);
        }
    }

    void outputPatchStats(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray patches = jsonObject.getAsJsonArray("patches");
        Map<String, Set<String>> oriFinalMap = new HashMap<>();
        List<String> samePatchBugs = new ArrayList<>();
        List<String> duplicatePatch = new ArrayList<>();
        getPatchStats(patches, oriFinalMap, samePatchBugs, duplicatePatch);
        System.out.println("补丁总数：" + patches.size());
        System.out.println("----------补丁修改位置及其修复代码----------");
        FileTools.outputMap(oriFinalMap);
        System.out.println("----------重复补丁类型----------");
        System.out.println("修改前后无变化：" + samePatchBugs.size());
        System.out.println(samePatchBugs);
        System.out.println("重复补丁：" + duplicatePatch.size());
        System.out.println(duplicatePatch);
    }

    List<AstorPatchInfo> getPatchInfos(String buggyBase, String repairBase) {
        List<String> mapping = FileTools.readEachLine(buggyBase + repairBase + "/mapping");
        List<String> success = Arrays.asList(FileTools.readOneLine(buggyBase + repairBase + "/success_bugs").split(","));
        List<String> proj_ids = new ArrayList<>();
        for (String map : mapping) {
            String[] temp = map.split(",");
            if (success.contains(temp[0]))//successful bugs condition: success.contains(temp[0])；failed bugs condition: !success.contains(temp[0])
                proj_ids.add(temp[1] + "_" + temp[2]);
        }
        Integer[] failed = {4, 7, 16, 18, 20, 21, 23, 34, 37, 38, 40};
        List<AstorPatchInfo> patchInfos = new ArrayList<>();
        for (int i = 0; i < proj_ids.size(); i++) {
            String proj_id = proj_ids.get(i);
            AstorPatchInfo patchInfo = new AstorPatchInfo(proj_id.split("_")[0], proj_id.split("_")[1], success.get(i));
            patchInfos.add(patchInfo);
            String output_file = buggyBase + repairBase + proj_id.split("_")[0] + "/VBAPRMain-" + proj_id + "/astor_output.json";
            if (!FileTools.isFileExist(output_file)) {//
                patchInfo.setTestSuccess(false);
                continue;
            }
//            if (!Arrays.asList(failed).contains(i))
            patchInfo.setTestSuccess(true);

            JsonElement jsonElement = new JsonParser().parse(FileTools.readFileByLines(output_file));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray patches = jsonObject.getAsJsonArray("patches");
            Map<String, Set<String>> oriFinalMap = new HashMap<>();
            List<String> samePatchBugs = new ArrayList<>();
            List<String> duplicatePatch = new ArrayList<>();
            getPatchStats(patches, oriFinalMap, samePatchBugs, duplicatePatch);

            patchInfo.setPatchSize(patches.size());
            for (Map.Entry<String, Set<String>> map : oriFinalMap.entrySet()) {
                patchInfo.setOperatorSize(map.getKey(), map.getValue().size());
            }

            JsonObject general = jsonObject.getAsJsonObject("general");
            double total_time = general.getAsJsonPrimitive("TOTAL_TIME").getAsDouble();
            double engine_creation_time = general.getAsJsonPrimitive("ENGINE_CREATION_TIME") != null ?
                    general.getAsJsonPrimitive("ENGINE_CREATION_TIME").getAsDouble() : 0;
            patchInfo.setTotalTime(total_time);
            patchInfo.setEngineCreationTime(engine_creation_time);
        }
        return patchInfos;
    }

    @Test
    public void testOperation() {
        String buggyBase = "/home/liumengjiao/Desktop/";
        String repairBase = "VBAPRResult_exhausted_Edit4Exp_compiled/";
        String repairBase1 = "VBAPRResult/";
        List<AstorPatchInfo> patchInfos = getPatchInfos(buggyBase, repairBase);
        List<AstorPatchInfo> patchInfos1 = getPatchInfos(buggyBase, repairBase1);
        int sum = 0;
        int num = 0;
        Map<String, Integer> deduceMap = new HashMap<>();
        for (int i = 0; i < patchInfos.size(); i++) {
            AstorPatchInfo patchInfo = patchInfos.get(i);
            List<AstorPatchInfo> patchInfo1 = patchInfos1.stream().filter(e -> e.getProj().equals(patchInfo.getProj()) && e.getId().equals(patchInfo.getId())).collect(Collectors.toList());
            assert patchInfo1.size() == 1;
            Map<String, Integer> map1 = patchInfo1.get(0).getOperatorSizeMap();
            for (Map.Entry<String, Integer> entry1 : map1.entrySet()) {
                String key = entry1.getKey();
                if (!key.contains("Insert"))
                    continue;
                num += 1;
                int size1 = entry1.getValue();
                int size = patchInfo.getOperatorSize(key);
                int deduce = size - size1;
                sum += deduce;
                if (!deduceMap.containsKey(patchInfo.getProj() + patchInfo.getId())) {
                    deduceMap.put(patchInfo.getProj() + patchInfo.getId(), 0);
                }
                deduceMap.put(patchInfo.getProj() + patchInfo.getId(), deduceMap.get(patchInfo.getProj() + patchInfo.getId()) + deduce);
                System.out.println(patchInfo.getProj() + patchInfo.getId() + ": " + deduce);
            }
        }
        System.out.println((double) sum / num);
//        FileTools.outputMap(deduceMap);
        System.out.println(deduceMap.size());
        System.out.println("zero deduce: " + deduceMap.values().stream().filter(o -> o == 0).count());
        System.out.println("deduce: " + deduceMap.values().stream().filter(o -> o < 0).count());
        System.out.println("ascend: " + deduceMap.values().stream().filter(o -> o > 0).count());
    }


    @Test
    public void testBugsStats() {
        String buggyBase = "/mnt/workspace/";
        String repairBase = "VBAPRResult/";//_exhausted_Edit_compiled
        List<String> mapping = FileTools.readEachLine(buggyBase + repairBase + "/mapping");
        List<String> success = mapping;//Arrays.asList(FileTools.readOneLine(buggyBase + repairBase + "/success_bugs").split(","));
        List<String> proj_ids = new ArrayList<>();
        for (String map : mapping) {
            String[] temp = map.split(",");
            if (!success.contains(temp[0]))//successful bugs condition: success.contains(temp[0])；failed bugs condition: !success.contains(temp[0])
                proj_ids.add(temp[1] + "_" + temp[2]);
        }
        Integer[] failed = {4, 7, 16, 18, 20, 21, 23, 34, 37, 38, 40};
        List<AstorPatchInfo> patchInfos = new ArrayList<>();
        for (int i = 0; i < proj_ids.size(); i++) {
            String proj_id = proj_ids.get(i);
            AstorPatchInfo patchInfo = new AstorPatchInfo(proj_id.split("_")[0], proj_id.split("_")[1], success.get(i));
            patchInfos.add(patchInfo);
            String output_file = buggyBase + repairBase + proj_id.split("_")[0] + "/VBAPRMain-" + proj_id + "/astor_output.json";
            if (!FileTools.isFileExist(output_file)) {//
                patchInfo.setTestSuccess(false);
                continue;
            }
//            if (!Arrays.asList(failed).contains(i))
            patchInfo.setTestSuccess(true);

            JsonElement jsonElement = new JsonParser().parse(FileTools.readFileByLines(output_file));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray patches = jsonObject.getAsJsonArray("patches");
            Map<String, Set<String>> oriFinalMap = new HashMap<>();
            List<String> samePatchBugs = new ArrayList<>();
            List<String> duplicatePatch = new ArrayList<>();
            getPatchStats(patches, oriFinalMap, samePatchBugs, duplicatePatch);

            patchInfo.setPatchSize(patches.size());
            for (Map.Entry<String, Set<String>> map : oriFinalMap.entrySet()) {
                patchInfo.setOperatorSize(map.getKey(), map.getValue().size());
            }

            JsonObject general = jsonObject.getAsJsonObject("general");
            double total_time = general.getAsJsonPrimitive("TOTAL_TIME").getAsDouble();
            double engine_creation_time = general.getAsJsonPrimitive("ENGINE_CREATION_TIME") != null ?
                    general.getAsJsonPrimitive("ENGINE_CREATION_TIME").getAsDouble() : 0;
            String staus = general.getAsJsonPrimitive("OUTPUT_STATUS").getAsString();
            patchInfo.setTotalTime(total_time);
            patchInfo.setEngineCreationTime(engine_creation_time);
            patchInfo.setStatus(staus);
            System.out.println(proj_id);
            System.out.println("是否找到真实补丁：" + staus.equals("STOP_BY_PATCH_FOUND"));
            System.out.println("补丁总数：" + patches.size());
            System.out.println("补丁生成时间：" + total_time);
            System.out.println("准备时间：" + engine_creation_time);
            System.out.println("准备时间：" + engine_creation_time);

        }
        int successCount = (int) patchInfos.stream().filter(AstorPatchInfo::isTestSuccess).count();
        double totalTimes = patchInfos.stream().filter(AstorPatchInfo::isTestSuccess).mapToDouble(AstorPatchInfo::getTotalTime).sum();
        int patchGenCount = (int) patchInfos.stream().filter(AstorPatchInfo::isTestSuccess).filter(o -> "STOP_BY_PATCH_FOUND".equals(o.getStatus())).count();
        double engineCreationTimes = patchInfos.stream().filter(AstorPatchInfo::isTestSuccess).mapToDouble(AstorPatchInfo::getEngineCreationTime).sum();
        int totalPatches = patchInfos.stream().filter(AstorPatchInfo::isTestSuccess).filter(AstorPatchInfo::isPatchGen).mapToInt(AstorPatchInfo::getPatchSize).sum();
        System.out.println("--------------------");
        System.out.println("bug总数：" + patchInfos.size());
        System.out.println("成功运行数：" + successCount);
        successCount = successCount == 0 ? 1 : successCount;
        patchGenCount = patchGenCount == 0 ? 1 : patchGenCount;
        System.out.println("平均准备时间：" + engineCreationTimes / successCount);
        System.out.println("平均补丁生成时间：" + totalTimes / successCount);
        System.out.println("成功生成补丁数：" + patchGenCount);
        System.out.println("平均补丁数目：" + totalPatches / patchGenCount);
        System.out.println();
        int failedCount = (int) patchInfos.stream().filter(o -> !o.isTestSuccess()).count();
        if (failedCount != 0) {
            totalTimes = patchInfos.stream().filter(o -> !o.isTestSuccess()).mapToDouble(AstorPatchInfo::getTotalTime).sum();
            engineCreationTimes = patchInfos.stream().filter(o -> !o.isTestSuccess()).mapToDouble(AstorPatchInfo::getEngineCreationTime).sum();
            System.out.println("失败运行数：" + failedCount);
            System.out.println("平均准备时间：" + engineCreationTimes / failedCount);
            System.out.println("平均补丁生成时间：" + totalTimes / failedCount);
            patchGenCount = (int) patchInfos.stream().filter(o -> !o.isTestSuccess()).filter(AstorPatchInfo::isPatchGen).count();
            if (patchGenCount != 0) {
                totalPatches = patchInfos.stream().filter(o -> !o.isTestSuccess()).filter(AstorPatchInfo::isPatchGen).mapToInt(AstorPatchInfo::getPatchSize).sum();
                System.out.println("平均补丁数目：" + totalPatches / patchGenCount);
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        Map<String, List<String>> statusMap = new HashMap<>();
        for (AstorPatchInfo info : patchInfos) {
            stringBuilder.append("STOP_BY_PATCH_FOUND".equals(info.getStatus())).append(',')
                    .append(info.getMappingIdx()).append(',')
                    .append(info.getProj()).append(',')
                    .append(info.getId()).append(',')
                    .append('\n');
            if (!statusMap.containsKey(info.getStatus())) {
                statusMap.put(info.getStatus(), new ArrayList<>());
            }
            statusMap.get(info.getStatus()).add(info.getProj() + info.getId());
        }
        FileTools.writeToFile(stringBuilder.toString(), buggyBase + repairBase + "patchinfo_stats");
        FileTools.writeToFile(FileTools.getMap2String(statusMap).toString(), buggyBase + repairBase + "patch_stats");
        System.out.println(patchInfos);
    }

    String getKey(JsonPrimitive original_code, JsonPrimitive operator, JsonPrimitive line) {
        return operator.getAsString() + "-" + line.getAsString() + "-" + original_code.getAsString();
    }
}
