import os
from huggingface_hub import snapshot_download
from mediapipe.tasks.python.genai import converter

def main():
    print("Downloading Qwen2.5-0.5B-Instruct from Hugging Face...")
    model_path = snapshot_download(
        "Qwen/Qwen2.5-0.5B-Instruct",
        allow_patterns=["*.safetensors", "*.json", "tokenizer*"]
    )
    print(f"Model downloaded to {model_path}")

    print("Converting to MediaPipe format...")
    os.makedirs("converted_slm", exist_ok=True)

    config = converter.ConversionConfig(
        input_ckpt=model_path,
        ckpt_format="safetensors",
        model_type="QWEN",
        backend="cpu",
        output_dir="converted_slm",
        combine_file_only=False
    )

    try:
        converter.convert_checkpoint(config)
        print("Conversion complete. Output is in converted_slm/.")
    except Exception as e:
        print(f"Error converting model: {e}")

if __name__ == "__main__":
    main()
