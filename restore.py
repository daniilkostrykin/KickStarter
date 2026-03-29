import os
import re

txt_filename = "kickstarter.txt"
# Новая целевая папка для Java файлов
target_base_dir = os.path.join("src", "main", "java", "org", "example", "kickstarter")

with open(txt_filename, "r", encoding="utf-8") as f:
    content = f.read()

parts = content.split("Содержимое файла:")

for part in parts[1:]:
    lines = part.strip().split("\n")
    if not lines or not lines[0].strip():
        continue

    full_path = lines[0].strip()

    is_java = full_path.endswith(".java")
    is_resources = "src\\main\\resources" in full_path or "src/main/resources" in full_path

    if is_java:
        # Отрезаем старый путь до org.example и приклеиваем новый
        if "org\\example\\" in full_path:
            sub_path = full_path.split("org\\example\\")[1]
        elif "org/example/" in full_path:
            sub_path = full_path.split("org/example/")[1]
        else:
            continue

        rel_path = os.path.join(target_base_dir, sub_path)

    elif is_resources:
        # application.properties кладем как обычно в resources
        if "src\\main\\resources" in full_path:
            rel_path = full_path[full_path.find("src\\main\\resources"):]
        else:
            rel_path = full_path[full_path.find("src/main/resources"):]
    else:
        continue

    rel_path = os.path.normpath(rel_path)

    # Очищаем код от системного мусора
    code_content = "\n".join(lines[1:]).strip()
    code_content = re.sub(r'\\s*', '', code_content)

    # МАГИЯ: Автоматически меняем пакеты и импорты в коде!
    if is_java:
        code_content = re.sub(r'^package org\.example', 'package org.example.kickstarter', code_content, flags=re.MULTILINE)
        code_content = re.sub(r'^import org\.example', 'import org.example.kickstarter', code_content, flags=re.MULTILINE)

    # Создаем папки и сохраняем файл
    os.makedirs(os.path.dirname(rel_path), exist_ok=True)
    with open(rel_path, "w", encoding="utf-8") as file:
        file.write(code_content)

    print(f"✅ Готово: {rel_path}")

print("\n🚀 Успех! Файлы перенесены, а пакеты внутри кода обновлены под org.example.kickstarter.")