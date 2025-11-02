# DashScope Chat API 响应对象完整说明

## 概述

本文档描述了 DashScope Chat API（基于 SDK 2.20.0）的完整响应对象结构。响应对象是一个完整的JSON结构，包含生成的文本内容、工具调用信息、使用统计等所有数据。

---

## 实际响应示例

### 示例1：普通文本回复（包含所有字段）

```json
{
  "requestId": "req-902fee3b-f7f0-9a8c-96a1-6b4ea25af114",
  "output": {
    "text": null,
    "finish_reason": null,
    "choices": [
      {
        "index": 0,
        "message": {
          "phase": null,
          "role": "assistant",
          "content": [
            {
              "text": "我是阿里云开发的一款超大规模语言模型，我叫通义千问。我可以帮助你解答问题、提供信息、协助写作等。",
              "image_hw": [1920, 1080],
              "ocr_result": [
                {
                  "kv_result": [],
                  "words_info": [
                    {
                      "rotate_rect": [100, 50, 200, 30, 0],
                      "location": [0, 0, 200, 0, 200, 30, 0, 30],
                      "text": "文本行的内容"
                    }
                  ]
                }
              ]
            }
          ],
          "annotations": null,
          "extra": null,
          "status": null,
          "reasoning_content": null,
          "tool_calls": [
            {
              "index": 0,
              "id": "call_abc123xyz",
              "type": "function",
              "function": {
                "name": "get_weather",
                "arguments": "{\"city\":\"北京\",\"date\":\"2024-01-15\"}"
              }
            }
          ]
        },
        "finish_reason": "stop"
      }
    ]
  },
  "usage": {
    "input_tokens": 22,
    "output_tokens": 17,
    "total_tokens": 39
  }
}
```

### 示例2：工具调用响应（包含所有字段）

```json
{
  "requestId": "req-abc123def456ghi789",
  "output": {
    "text": null,
    "finish_reason": null,
    "choices": [
      {
        "index": 0,
        "message": {
          "role": "assistant",
          "content": null,
          "tool_calls": [
            {
              "id": "call_abc123xyz",
              "type": "function",
              "function": {
                "name": "get_weather",
                "arguments": "{\"city\":\"北京\",\"date\":\"2024-01-15\"}"
              }
            },
            {
              "id": "call_def456uvw",
              "type": "function",
              "function": {
                "name": "calculate_sum",
                "arguments": "{\"a\":10,\"b\":20}"
              }
            }
          ]
        },
        "finish_reason": "tool_calls"
      }
    ]
  },
  "usage": {
    "input_tokens": 145,
    "output_tokens": 23,
    "total_tokens": 168
  }
}
```

### 示例3：多个生成选项（n > 1，包含所有字段和不同finish_reason）

```json
{
  "requestId": "req-multiple-choices-123",
  "output": {
    "text": null,
    "finish_reason": null,
    "choices": [
      {
        "index": 0,
        "message": {
          "role": "assistant",
          "content": "第一个回复选项的内容，正常完成。",
          "tool_calls": null
        },
        "finish_reason": "stop"
      },
      {
        "index": 1,
        "message": {
          "role": "assistant",
          "content": "第二个回复选项的内容，这是一个很长的回复内容，但由于达到了最大token限制...",
          "tool_calls": null
        },
        "finish_reason": "length"
      },
      {
        "index": 2,
        "message": {
          "role": "assistant",
          "content": null,
          "tool_calls": null
        },
        "finish_reason": "content_filter"
      }
    ]
  },
  "usage": {
    "input_tokens": 50,
    "output_tokens": 150,
    "total_tokens": 200
  }
}
```