from PIL import Image, ImageDraw
import os

def generate_adaptive_icon_parts(input_path, output_dir):
    """
    生成自适应图标的背景和前景层
    
    Args:
        input_path: 输入图片路径（应该是 1024x1024 或 512x512）
        output_dir: 输出目录
    """
    # 打开原图
    img = Image.open(input_path).convert("RGBA")
    
    # 背景尺寸（自适应图标的安全区域）
    SAFE_ZONE = 512  # 安全区域的尺寸（1024x1024 的图标中，安全区域是中间的 512x512）
    
    # 调整图像大小到安全区域
    original_size = img.size[0]
    
    # 计算裁剪区域（居中裁剪到安全区域）
    if original_size > SAFE_ZONE:
        crop_box = (
            (original_size - SAFE_ZONE) // 2,
            (original_size - SAFE_ZONE) // 2,
            (original_size - SAFE_ZONE) // 2 + SAFE_ZONE,
            (original_size - SAFE_ZONE) // 2 + SAFE_ZONE
        )
        img = img.crop(crop_box)
    
    # 定义不同密度的尺寸
    densities = {
        "mipmap-mdpi": 108,
        "mipmap-hdpi": 162,
        "mipmap-xhdpi": 216,
        "mipmap-xxhdpi": 324,
        "mipmap-xxxhdpi": 432
    }
    
    for folder_name, size in densities.items():
        output_folder = os.path.join(output_dir, folder_name)
        os.makedirs(output_folder, exist_ok=True)
        
        # 创建背景层（108dp × 108dp = 324px for xxxhdpi）
        background = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        foreground = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        
        # 调整前景图片大小
        foreground_img = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # 将前景图片粘贴到前景层（居中）
        foreground.paste(foreground_img, (0, 0), foreground_img if foreground_img.mode == 'RGBA' else None)
        
        # 保存前景层
        foreground_path = os.path.join(output_folder, "ic_launcher_foreground.png")
        foreground.save(foreground_path, 'PNG', optimize=True)
        print(f"[OK] 生成前景: {foreground_path} ({size}x{size})")
        
        # 背景层可以是纯色或渐变
        # 创建渐变背景（从浅灰到白色）
        draw = ImageDraw.Draw(background)
        for y in range(size):
            # 计算渐变颜色值
            ratio = y / size
            color = int(240 + 15 * ratio)  # 从浅灰到白色
            draw.line([(0, y), (size, y)], fill=(color, color, color, 255))
        
        background_path = os.path.join(output_folder, "ic_launcher_background.png")
        background.save(background_path, 'PNG', optimize=True)
        print(f"[OK] 生成背景: {background_path} ({size}x{size})")


if __name__ == "__main__":
    # 输入输出路径
    default_input = "app/src/main/res/ic_launcher.png"
    default_output = "app/src/main/res"
    
    print("=" * 50)
    print("Android自适应图标生成脚本")
    print("=" * 50)
    print(f"输入文件: {default_input}")
    print(f"输出目录: {default_output}")
    print()
    
    if not os.path.exists(default_input):
        print(f"[ERROR] 输入文件不存在: {default_input}")
        exit(1)
    
    generate_adaptive_icon_parts(default_input, default_output)
    
    print("\n[SUCCESS] 自适应图标生成完成！")
    print("\n生成的资源文件：")
    print("  - ic_launcher_foreground.png (前景层)")
    print("  - ic_launcher_background.png (背景层)")
    print("\n目录结构：")
    print("  res/mipmap-anydpi-v26/ic_launcher.xml")
    print("  res/mipmap-anydpi-v26/ic_launcher_round.xml")
    print("  res/mipmap-*/ic_launcher_background.png")
    print("  res/mipmap-*/ic_launcher_foreground.png")

