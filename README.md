# AI Model Aggregator (AI模型聚合助手)

An Android app that aggregates 20+ AI large language model providers into a single unified interface. Chat, generate code, create images, and compare models side-by-side — all from one app.

## Features

### Chat (对话)
- Text generation with streaming output for real-time responses
- Adjustable parameters: Temperature, Top-P, and Max Tokens
- Full conversation history stored locally
- Support for 100+ models across 20 vendors

### Code Generation (代码)
- Generate code in 17 programming languages
- Supported languages: Python, Java, Kotlin, JavaScript, TypeScript, Go, Rust, C, C++, C#, Swift, Ruby, PHP, SQL, HTML, CSS, Shell
- Copy to clipboard with one tap
- Export code as `.txt` files via share sheet

### Image Generation (图片)
- AI-powered text-to-image generation
- Multiple aspect ratios: 1:1 (Square), 3:4 (Portrait), 4:3 (Landscape), 9:16 (Vertical), 16:9 (Widescreen)
- Generate 1–4 images per request
- In-app image preview with zoom

### Cluster Comparison (集群)
- Query multiple models simultaneously with the same prompt
- Compare responses side-by-side in real-time
- Select any combination of models for comparison
- Independent streaming for each model

### History & Settings (历史)
- Browse all past conversations
- Configure API keys for all 20 vendors individually
- About section with app information

## Supported Vendors

### Domestic (15)
| Vendor | Key Models |
|--------|------------|
| Baidu (百度) | ERNIE 4.5 Turbo, ERNIE 4.0 Turbo, ERNIE 3.5, ERNIE-Speed |
| Alibaba (阿里巴巴) | Qwen-Max, Qwen-Plus, Qwen-Turbo, Qwen-VL |
| DeepSeek (深度求索) | DeepSeek-V3, DeepSeek-R1, DeepSeek-Coder |
| ByteDance (字节跳动) | Doubao-Pro, Doubao-Lite, Skylark |
| Tencent (腾讯) | Hunyuan-Pro, Hunyuan-Lite, Hunyuan-Vision |
| Zhipu AI (智谱AI) | GLM-4-Plus, GLM-4, GLM-4V, CodeGeeX |
| iFlytek (科大讯飞) | Spark 4.0 Ultra, Spark Max, Spark Pro |
| SenseTime (商汤) | SenseNova 5.0, SenseChat, SenseVision |
| 360 AI | 360GPT-V2, 360GPT-Pro, 360GPT-Lite |
| Kunlun (昆仑万维) | Skywork-4.0, Skywork-3.0, Skywork-Coder |
| Moonshot AI (月之暗面) | Kimi, Moonshot-v1-8k/32k/128k |
| Baichuan (百川智能) | Baichuan4, Baichuan3-Turbo, Baichuan2 |
| 01.AI (零一万物) | Yi-Large, Yi-Medium, Yi-Vision |
| Nebula (智云) | Nebula-Pro, Nebula-Lite, Nebula-Coder |
| CAS (中科院) | Taichu-2.0, Taichu-Vision, Taichu-Coder |

### International (5)
| Vendor | Key Models |
|--------|------------|
| OpenAI | GPT-4o, GPT-4 Turbo, GPT-3.5 Turbo, DALL-E 3 |
| Anthropic | Claude 3.5 Sonnet, Claude 3 Opus, Claude 3 Haiku |
| Google | Gemini 1.5 Pro, Gemini 1.5 Flash, Gemini Ultra |
| Meta | Llama 3.1 405B, Llama 3.1 70B, Llama 3 8B |
| Mistral AI | Mistral Large, Mistral Medium, Mistral Small, Codestral |

## Tech Stack

- **Language**: Kotlin
- **UI**: Material Design 3, ViewBinding, MaterialCardView
- **HTTP Client**: OkHttp 4.x (streaming, retry, timeout)
- **JSON**: Gson
- **Image Loading**: Glide 4.x
- **Local Storage**: SharedPreferences (API keys), SQLite (planned)
- **Build**: Gradle 8.x, AGP 8.2.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
ai_model_aggregator/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/aiaggregator/app/
│       │   ├── MainActivity.kt              # App entry with bottom nav
│       │   ├── models/
│       │   │   └── Models.kt                # Data classes
│       │   ├── data/
│       │   │   ├── VendorConfig.kt          # 20 vendors, 100+ models
│       │   │   └── ApiService.kt            # Unified API client
│       │   └── ui/
│       │       ├── home/HomeFragment.kt     # Chat interface
│       │       ├── code/CodeFragment.kt     # Code generation
│       │       ├── image/ImageFragment.kt   # Image generation
│       │       ├── cluster/ClusterFragment.kt # Multi-model comparison
│       │       └── history/HistoryFragment.kt # History & settings
│       └── res/
│           ├── layout/                      # XML layouts
│           ├── drawable/                    # Icons & backgrounds
│           ├── values/                      # Colors, strings, themes
│           └── menu/                        # Bottom nav menu
├── build.gradle
├── settings.gradle
├── gradle.properties
├── app-debug.apk                            # Pre-built APK
└── README.md
```

## Getting Started

### Prerequisites
- JDK 17
- Android SDK 34 (platforms + build-tools 34.0.0)
- Gradle 8.x

### Build from Source

```bash
# Clone the repository
git clone https://github.com/x1449237/AI-model-api-frontend.git
cd AI-model-api-frontend

# Build the APK
./gradlew assembleDebug

# APK output location
# app/build/outputs/apk/debug/app-debug.apk
```

### Install via APK

1. Download `app-debug.apk` from the repository root or [Releases](https://github.com/x1449237/AI-model-api-frontend)
2. On your Android device, enable **Settings > Security > Unknown Sources**
3. Open the APK file to install

## Usage Guide

### 1. Configure API Keys
Before using any model, you need to configure its API key:
1. Go to the **History (历史)** tab
2. Switch to the **My (我的)** sub-tab
3. Enter your API key for the desired vendor
4. API keys are stored locally on your device only

### 2. Chat with a Model
1. Select the **Chat (对话)** tab
2. Choose a vendor and model from the chips at the top
3. Adjust Temperature, Top-P, and Max Tokens via the tuning icon
4. Type your message and tap send
5. Watch the response stream in real-time

### 3. Generate Code
1. Select the **Code (代码)** tab
2. Choose a programming language from the chip selector
3. Pick a model for code generation
4. Describe what code you need
5. Copy the result or export it as a file

### 4. Generate Images
1. Select the **Image (图片)** tab
2. Choose an image-capable model (e.g., DALL-E 3, ERNIE Image)
3. Write your image description prompt
4. Select aspect ratio and number of images
5. Tap generate and view results in the gallery

### 5. Compare Models (Cluster Mode)
1. Select the **Cluster (集群)** tab
2. Check the models you want to compare
3. Enter a common prompt
4. Send — all selected models will respond simultaneously
5. Compare outputs side-by-side

## API Key Acquisition

| Vendor | Registration URL |
|--------|------------------|
| OpenAI | https://platform.openai.com/api-keys |
| Anthropic | https://console.anthropic.com |
| Google | https://aistudio.google.com/apikey |
| DeepSeek | https://platform.deepseek.com |
| Zhipu AI | https://open.bigmodel.cn |
| Alibaba | https://dashscope.console.aliyun.com |
| Baidu | https://console.bce.baidu.com |
| ByteDance | https://console.volcengine.com/ark |
| Moonshot AI | https://platform.moonshot.cn |
| Baichuan | https://platform.baichuan-ai.com |
| 01.AI | https://platform.lingyiwanwu.com |
| Meta | https://meta.ai |
| Mistral AI | https://console.mistral.ai |

## License

This project is for educational and personal use. All API services belong to their respective providers.

## Disclaimer

This app requires users to provide their own API keys for each vendor. API usage may incur costs based on each provider's pricing. The app does not collect or transmit any personal data — all API keys and conversations are stored locally on your device.