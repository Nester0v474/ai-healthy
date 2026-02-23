# Как выложить проект на GitHub

1. **Установи Git** (если ещё нет): https://git-scm.com/download/win

2. **Создай репозиторий на GitHub:**  
   Зайди на https://github.com/new  
   Название, например: `ai-healthy`  
   Не добавляй README, .gitignore и лицензию — они уже есть в проекте.

3. **В папке проекта выполни в терминале (PowerShell или cmd):**

```bash
cd "D:\Ai Healthy"
git init
git add .
git commit -m "Ai Healthy: Android + Node.js backend, без Ru Store"
git branch -M main
git remote add origin https://github.com/Nester0v474/ai-healthy.git
git push -u origin main
```

Замени `Nester0v474/ai-healthy` на свой логин и имя репозитория, если другое.

4. Если репозиторий уже создан с README, перед первым push сделай:
```bash
git pull origin main --allow-unrelated-histories
git push -u origin main
```
