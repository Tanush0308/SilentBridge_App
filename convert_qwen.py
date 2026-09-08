import os
from mediapipe.tasks.python.genai.converter import llm_converter
from huggingface_hub import snapshot_download

def main():
    model_id = "Qwen/Qwen2.5-0.5B-Instruct"
    print(f"Downloading {model_id} from Hugging Face...")
    try:
        model_path = snapshot_download(repo_id=model_id, allow_patterns=["*.safetensors", "*.json", "*.txt"])
        print(f"Model downloaded to {model_path}. Starting conversion...")
        
        output_dir = os.path.abspath(".")
        output_task = os.path.join(output_dir, "qwen.task")
        
        config = llm_converter.ConversionConfig(
            input_ckpt=model_path,
            ckpt_format="safetensors",
            model_type="QWEN",
            backend="cpu",
            output_dir=output_dir,
            combine_file_only=False,
            output_tflite_file=output_task
        )
        
        llm_converter.convert_checkpoint(config)
        print(f"Successfully converted to {output_task}")
    except Exception as e:
        print(f"Error during conversion: {e}")

if __name__ == "__main__":
    main()
