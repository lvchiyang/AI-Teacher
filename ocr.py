import requests
import imghdr
import tempfile
import os
import logging
from openai import OpenAI
from bs4 import BeautifulSoup
from urllib.parse import urlparse

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def grade_homework_func(image_url: str) -> str:
            """
            作业批改工具的工作流程：
            1. 对输入图片进行预处理，例如去噪，二值化
            2. 使用"qwen-vl-ocr"api对图片进行识别与批改
            """
            try:
                # 将image_url作为URL处理，并优化代码健壮性                
                # 验证URL格式
                parsed_url = urlparse(image_url)
                if not parsed_url.scheme or not parsed_url.netloc:
                    return f"错误：无效的URL格式 {image_url}"
                
                # 发送HTTP请求获取内容
                try:
                    response = requests.get(image_url, timeout=30)
                    response.raise_for_status()  # 检查HTTP错误
                except requests.exceptions.RequestException as e:
                    return f"错误：无法访问URL {image_url}，详情: {str(e)}"
                
                content_type = response.headers.get('content-type', '').lower()
                
                # 检查是否为直接的图片链接
                if content_type.startswith('image/'):
                    # 直接下载图片到临时文件
                    with tempfile.NamedTemporaryFile(delete=False) as tmp_file:
                        tmp_file.write(response.content)
                        temp_image_path = tmp_file.name
                else:
                    # 处理网页链接，从中提取图片
                    soup = BeautifulSoup(response.text, 'html.parser')
                    images = soup.find_all('img')
                    
                    if not images:
                        return f"错误：网页 {image_url} 中未找到图片"
                    
                    # 获取第一张图片的URL
                    first_img = images[0]
                    img_src = first_img.get('src') or first_img.get('data-src')  # 支持懒加载图片
                    
                    if not img_src:
                        return f"错误：网页 {image_url} 中的图片没有有效的src属性"
                    
                    # 处理相对路径
                    if img_src.startswith('//'):
                        img_src = 'https:' + img_src
                    elif img_src.startswith('/'):
                        # 相对路径处理
                        parsed_base = urlparse(image_url)
                        img_src = f"{parsed_base.scheme}://{parsed_base.netloc}{img_src}"
                    elif not img_src.startswith(('http://', 'https://')):
                        # 相对路径处理
                        parsed_base = urlparse(image_url)
                        base_path = '/'.join(parsed_base.path.split('/')[:-1]) + '/'
                        img_src = f"{parsed_base.scheme}://{parsed_base.netloc}{base_path}{img_src}"
                    
                    # 下载图片
                    try:
                        img_response = requests.get(img_src, timeout=30)
                        img_response.raise_for_status()
                        
                        # 检查下载的内容是否为图片
                        if not img_response.headers.get('content-type', '').lower().startswith('image/'):
                            return f"错误：从网页中提取的链接 {img_src} 不是有效的图片"
                        
                        # 保存图片到临时文件
                        with tempfile.NamedTemporaryFile(delete=False) as tmp_file:
                            tmp_file.write(img_response.content)
                            temp_image_path = tmp_file.name
                            
                    except requests.exceptions.RequestException as e:
                        return f"错误：无法下载图片 {img_src}，详情: {str(e)}"
                
                # 验证临时文件是否为有效图片
                if not imghdr.what(temp_image_path):
                    os.unlink(temp_image_path)  # 删除临时文件
                    return f"错误：下载的文件不是有效的图片格式"
                
                # 检查API密钥
                api_key = os.getenv("DASHSCOPE_API_KEY")
                if not api_key:
                    os.unlink(temp_image_path)  # 删除临时文件
                    logging.error("未设置DASHSCOPE_API_KEY环境变量")
                    return "错误：未设置DASHSCOPE_API_KEY环境变量"
                
                
                
                # 删除临时文件
                os.unlink(temp_image_path)
                
                client = OpenAI(
                    api_key=api_key,
                    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
                )

                call_params = {
                    "model": "qwen-vl-ocr",
                    "messages": [
                        {
                            "role": "user",
                            "content": [
                                {
                                    "type": "image_url",
                                    "image_url": {
                                        "url": image_url
                                    },
                                },
                                {"type": "text", "text": "请仅输出图像中的文本内容。"},
                            ],
                        },
                        {
                            "role": "assistant",
                            "content": [
                                {
                                    "type": "text",
                                    "text": "请将图像中的题目内容识别出来，进行批改,并返回批改结果。"
                                }
                            ]
                        }
                    ],
                    "stream": False
                }
                
                completion = client.chat.completions.create(**call_params)

                result = completion.choices[0].message
                
                return result
                
            except Exception as e:
                # 确保临时文件被清理
                if 'temp_image_path' in locals():
                    try:
                        os.unlink(temp_image_path)
                    except:
                        pass
                return f"作业批改过程中出现错误: {str(e)}"