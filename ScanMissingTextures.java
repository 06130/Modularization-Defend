import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 扫描缺失的物品纹理文件
 */
public class ScanMissingTextures {
    
    // 资源根目录
    private static final String ASSETS_ROOT = "src/main/resources/assets/modularization_defend";
    private static final String MODELS_ITEM_DIR = ASSETS_ROOT + "/models/item";
    private static final String TEXTURES_ITEM_DIR = ASSETS_ROOT + "/textures/item";
    
    // Java源码目录
    private static final String JAVA_ITEMS_FILE = "src/main/java/org/lingZero/m_defend/Register/ModItems.java";
    
    public static void main(String[] args) {
        System.out.println("=== 开始扫描缺失的物品纹理文件 ===\n");
        
        // 1. 从 ModItems.java 中提取所有注册的物品名称
        List<String> registeredItems = extractRegisteredItems();
        System.out.println("已注册物品数量: " + registeredItems.size());
        System.out.println("物品列表: " + registeredItems);
        System.out.println();
        
        // 2. 检查每个物品的模型文件和纹理文件
        List<String> missingModels = new ArrayList<>();
        List<String> missingTextures = new ArrayList<>();
        List<String> existingItems = new ArrayList<>();
        
        for (String itemName : registeredItems) {
            String modelPath = MODELS_ITEM_DIR + "/" + itemName + ".json";
            String texturePath = TEXTURES_ITEM_DIR + "/" + itemName + ".png";
            
            boolean hasModel = Files.exists(Paths.get(modelPath));
            boolean hasTexture = Files.exists(Paths.get(texturePath));
            
            if (!hasModel) {
                missingModels.add(itemName);
            }
            if (!hasTexture) {
                missingTextures.add(itemName);
            }
            if (hasModel && hasTexture) {
                existingItems.add(itemName);
            }
        }
        
        // 3. 输出结果
        System.out.println("=== 检查结果 ===\n");
        
        System.out.println("✓ 完整的物品 (有模型和纹理): " + existingItems.size());
        for (String item : existingItems) {
            System.out.println("  - " + item);
        }
        System.out.println();
        
        if (!missingModels.isEmpty()) {
            System.out.println("✗ 缺失模型文件的物品: " + missingModels.size());
            for (String item : missingModels) {
                String modelPath = MODELS_ITEM_DIR + "/" + item + ".json";
                System.out.println("  - " + item);
                System.out.println("    需要创建: " + modelPath);
            }
            System.out.println();
        } else {
            System.out.println("✓ 所有物品都有模型文件\n");
        }
        
        if (!missingTextures.isEmpty()) {
            System.out.println("✗ 缺失纹理文件的物品: " + missingTextures.size());
            for (String item : missingTextures) {
                String texturePath = TEXTURES_ITEM_DIR + "/" + item + ".png";
                System.out.println("  - " + item);
                System.out.println("    需要创建: " + texturePath);
            }
            System.out.println();
        } else {
            System.out.println("✓ 所有物品都有纹理文件\n");
        }
        
        // 4. 生成缺失文件的创建建议
        if (!missingModels.isEmpty() || !missingTextures.isEmpty()) {
            System.out.println("=== 建议操作 ===\n");
            
            if (!missingModels.isEmpty()) {
                System.out.println("需要创建的模型文件:");
                for (String item : missingModels) {
                    System.out.println(generateModelJson(item));
                }
                System.out.println();
            }
            
            if (!missingTextures.isEmpty()) {
                System.out.println("需要创建的纹理文件 (PNG格式):");
                for (String item : missingTextures) {
                    System.out.println("  - " + TEXTURES_ITEM_DIR + "/" + item + ".png");
                }
                System.out.println();
            }
        }
        
        System.out.println("=== 扫描完成 ===");
    }
    
    /**
     * 从 ModItems.java 文件中提取所有注册的物品名称
     */
    private static List<String> extractRegisteredItems() {
        List<String> items = new ArrayList<>();
        
        try {
            String content = new String(Files.readAllBytes(Paths.get(JAVA_ITEMS_FILE)));
            
            // 匹配 ITEMS.register 或 ITEMS.registerItem 或 ITEMS.registerSimpleItem 等模式
            // 提取第一个参数（物品名称）
            Pattern pattern = Pattern.compile(
                "ITEMS\\.register(?:Item|SimpleItem|SimpleBlockItem)?\\s*\\(\\s*\"([^\"]+)\""
            );
            Matcher matcher = pattern.matcher(content);
            
            while (matcher.find()) {
                String itemName = matcher.group(1);
                items.add(itemName);
            }
            
        } catch (Exception e) {
            System.err.println("读取文件失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * 生成物品模型JSON内容
     */
    private static String generateModelJson(String itemName) {
        return String.format(
            "文件: %s/models/item/%s.json\n" +
            "内容:\n" +
            "{\n" +
            "  \"parent\": \"minecraft:item/generated\",\n" +
            "  \"textures\": {\n" +
            "    \"layer0\": \"modularization_defend:item/%s\"\n" +
            "  }\n" +
            "}\n",
            ASSETS_ROOT, itemName, itemName
        );
    }
}
