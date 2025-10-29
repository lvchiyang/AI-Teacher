#!/usr/bin/env python3
"""
应用图标生成脚本
将450x450的图片生成不同密度的图标并保存到对应的mipmap文件夹
"""

from PIL import Image
import os
import sys

# 图标尺寸定义（Android标准）
ICON_SIZES = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

def generate_icons(input_path, output_base_dir):
    """
    生成不同尺寸的图标
    
    Args:
        input_path: 输入图片路径
        output_base_dir: 输出基础目录（通常是app/src/main/res）
    """
    # 打开图片
    try:
        img = Image.open(input_path)
        print(f"[OK] 成功打开图片: {input_path}")
        print(f"  原始尺寸: {img.size[0]}x{img.size[1]}")
    except Exception as e:
        print(f"[ERROR] 无法打开图片: {e}")
        return False
    
    # 确保图片是RGBA模式（支持透明度）
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    
    # 生成的图标文件名
    output_filename = "ic_launcher.png"
    
    # 为每个密度生成图标
    success_count = 0
    for folder_name, size in ICON_SIZES.items():
        output_dir = os.path.join(output_base_dir, folder_name)
        output_path = os.path.join(output_dir, output_filename)
        
        try:
            # 确保输出目录存在
            os.makedirs(output_dir, exist_ok=True)
            
            # 调整图片大小（使用高质量的重采样方法）
            resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
            
            # 保存图片
            resized_img.save(output_path, 'PNG', optimize=True)
            print(f"[OK] 生成成功: {output_path} ({size}x{size})")
            success_count += 1
        except Exception as e:
            print(f"[ERROR] 生成失败 {folder_name}: {e}")
    
    print(f"\n总共生成 {success_count}/{len(ICON_SIZES)} 个图标")
    return success_count == len(ICON_SIZES)


if __name__ == "__main__":
    # 默认输入输出路径
    default_input = "app/src/main/res/ic_launcher.png"
    default_output = "app/src/main/res"
    
    # 如果有命令行参数，使用参数
    if len(sys.argv) >= 2:
        default_input = sys.argv[1]
    if len(sys.argv) >= 3:
        default_output = sys.argv[2]
    
    print("=" * 50)
    print("Android应用图标生成工具")
    print("=" * 50)
    print(f"输入文件: {default_input}")
    print(f"输出目录: {default_output}")
    print()
    
    # 检查输入文件是否存在
    if not os.path.exists(default_input):
        print(f"✗ 错误: 输入文件不存在: {default_input}")
        sys.exit(1)
    
    # 生成图标
    success = generate_icons(default_input, default_output)
    
    if success:
        print("\n✓ 所有图标生成完成！")
    else:
        print("\n✗ 图标生成过程中出现错误")
        sys.exit(1)

