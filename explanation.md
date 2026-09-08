# SilentBridge Project Overview & Architecture

## 1. Project Vision & Core Purpose
**SilentBridge** is an accessibility-focused Android application designed to bridge the communication gap between sign language users and non-signers. By leveraging specialized hardware (sensor-equipped gloves) and cutting-edge on-device AI, the app translates continuous sign language gestures into natural, spoken sentences in real-time—completely offline.

---

## 2. Implemented Features

### 📡 Hardware-Integrated Gesture Capture
- **Bluetooth Sensor Gloves:** Instead of relying on a camera, the app connects to custom Bluetooth gloves that stream IMU (Accelerometer and Gyroscope) data.
- **Real-time `GestureEngine`:** Processes the continuous stream of `SensorFrame` data, using motion detection to segment distinct gestures automatically.

### 🧠 On-Device AI Pipeline (Offline-First)
- **TFLite Gesture Classification:** A lightweight, on-device TensorFlow Lite model predicts the intended word or sign from the segmented IMU data.
- **Gemini Nano (SLM) Sentence Reconstruction:** Raw sign language often lacks connective words (e.g., signing "I", "Hungry"). SilentBridge uses an on-device Small Language Model (SLM) via `LanguageEngine` to reconstruct raw keywords into grammatically correct, natural sentences (e.g., "I am hungry").
- **Semantic Validation & Intent Interpretation:** Ensures the reconstructed sentence makes logical sense and aligns with the user's original gesture intent before speaking.

### ⚙️ Capture Modes
- **Auto Mode:** Continuously recognizes gestures and automatically triggers sentence generation when a pause is detected. Built for fluid, rapid communication.
- **Manual Mode:** A guided mode where the user captures a sign, and the app asks for confirmation. If the app guesses wrong, the user can correct it. 

### 🔄 Adaptive Learning & Feedback Loop
- **Penalty-based Correction (`FeedbackRepository`):** When a user corrects a misclassified sign in Manual mode, the app logs this. If a specific sign is repeatedly misclassified, the system applies a confidence penalty to that prediction in the future, effectively "adapting" to the user's specific gesture style over time.

### 🌍 Multi-language TTS & Translation
- **Offline ML Kit Translation:** Translates the generated English sentences into various target languages completely offline.
- **Text-to-Speech (TTS):** Speaks the final translated sentence aloud, serving as the user's digital voice.

### 📚 Educational Tutorials
- **ASL Video Library:** A dedicated learning section built into the app that uses a WebView to play verified YouTube ASL tutorials. It utilizes Coil to efficiently load video thumbnails, helping new users or family members learn sign language.

---

## 3. Technology Stack: What We Used and Why

| Technology | Why We Used It |
| :--- | :--- |
| **Kotlin & Jetpack Compose** | Provides a modern, reactive UI. Compose allows us to easily manage complex UI states (like switching between Auto/Manual modes and showing real-time word buffers) with less boilerplate. |
| **Google MediaPipe Tasks / Gemini Nano** | Used for the `LlmInference` engine. We chose this because it runs a powerful LLM natively on the Android device without requiring internet, ensuring low latency and absolute privacy. |
| **TensorFlow Lite (TFLite)** | Powers the gesture recognition classifier. It is optimized for mobile CPUs/Edge devices, allowing us to process IMU sensor data at high frame rates without draining the battery. |
| **Android Bluetooth/BLE** | Essential for maintaining a low-latency, stable connection with the hardware gloves. |
| **Coil** | Used for async image loading in the Tutorials UI. It is lightweight, Kotlin-first, and integrates seamlessly with Jetpack Compose. |

---

## 4. What We Did NOT Use and Why

- **Cloud APIs (OpenAI, AWS, GCP):** We actively avoided sending gesture data or text to the cloud for inference. **Why?** Accessibility tools must work everywhere—in subways, rural areas, or places with spotty internet. Cloud APIs introduce latency and ongoing costs, whereas our offline-first approach guarantees instant, free, and private communication.
- **Camera-Based Vision Models (MediaPipe Hands via Camera):** We chose IMU sensor gloves over computer vision. **Why?** Camera-based recognition suffers in low light, struggles with occlusion (hands blocking each other), and drains the battery heavily. More importantly, having a camera constantly pointed at the user raises severe privacy concerns. Sensor gloves are private, reliable in any lighting, and computationally cheaper.
- **Heavy Frameworks (React Native / Flutter):** We avoided cross-platform UI frameworks. **Why?** Integrating deeply with low-level Android Bluetooth APIs, TFLite, and local on-device SLMs requires tight native integration. Native Kotlin provides the performance and hardware access we critically need.

---

## 5. Future Roadmap (Features To Be Implemented)

1. **Custom Sign Few-Shot Learning (Currently in Development):**
   - **What it is:** Allowing users to create and train their own custom signs directly on their device. If a user has a specific sign for a niche term (e.g., their team name "Le'Squad" or a unique family nickname), the app will learn it.
   - **How it will work:** The user records the gesture a few times in the app. The app stores the kinematic template in a local `CustomSignCache`. During inference, a `CustomSignClassifier` will compare live gestures against these custom templates using Cosine Similarity, prioritizing the user's custom dictionary over the base model.
2. **Two-Way Communication (Voice-to-Text/Sign):**
   - Enabling the app to listen to spoken language from the non-signer and transcribe it to text on the screen, creating a complete two-way dialogue loop.
3. **Expanded Core Vocabulary:**
   - Continuously upgrading the base TFLite model to support hundreds of new everyday words based on generalized IMU datasets.
4. **Cloud Backup for Custom Signs:**
   - An optional opt-in feature to sync a user's custom trained signs across their personal devices using Firebase.

---

## 6. Summary for the Team
SilentBridge is not just a standard Android app; it is a **hardware-software ecosystem**. We have built a robust, offline-first pipeline that takes raw physical physics data (accelerometer/gyro), translates it into words via Edge AI, and reconstructs those words into human-sounding sentences using a mobile-optimized LLM. By prioritizing privacy, offline availability, and adaptive learning, we've created a deeply personal accessibility tool that grows and adapts with its user.
