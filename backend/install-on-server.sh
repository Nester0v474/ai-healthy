#!/bin/bash

set -e
mkdir -p /root/backend
cd /root/backend

echo "Создаю package.json..."
cat > package.json << 'EOF'
{"name":"ai-healthy-backend","version":"1.0.0","description":"Backend for AI Healthy app","main":"server.js","scripts":{"start":"node server.js"},"dependencies":{"express":"^4.18.2","cors":"^2.8.5","axios":"^1.6.0","dotenv":"^16.3.1"}}
EOF

echo "Создаю server.js..."
cat > server.js << 'ENDOFFILE'
const express = require('express');
const cors = require('cors');
const axios = require('axios');
require('dotenv').config();
const app = express();
const PORT = process.env.PORT || 3000;
app.use(cors());
app.use(express.json());
app.use((req, res, next) => {
  console.log(new Date().toISOString(), req.method, req.path);
  next();
});
const DEEPSEEK_API_KEY = process.env.DEEPSEEK_API_KEY;
if (!DEEPSEEK_API_KEY) {
  console.error('ERROR: DEEPSEEK_API_KEY not set in .env');
  process.exit(1);
}
app.post('/api/chat', async (req, res) => {
  try {
    const { model, messages, temperature, max_tokens, stream } = req.body;
    if (!messages || !Array.isArray(messages) || messages.length === 0) {
      return res.status(400).json({ error: { message: 'messages is required' } });
    }
    const response = await axios.post('https://api.deepseek.com/v1/chat/completions', {
      model: model || 'deepseek-chat',
      messages,
      temperature: temperature || 0.7,
      max_tokens: max_tokens || 2000,
      stream: stream || false
    }, {
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + DEEPSEEK_API_KEY },
      timeout: 60000
    });
    res.json(response.data);
  } catch (error) {
    console.error('DeepSeek Error:', error.message);
    res.status(500).json({ error: { message: error.message || 'API error' } });
  }
});
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});
app.listen(PORT, '0.0.0.0', () => {
  console.log('Server on port', PORT);
});
ENDOFFILE

echo "Создаю .env.example..."
echo -e "PORT=3000\nDEEPSEEK_API_KEY=sk-твой_ключ" > .env.example

if [ ! -f .env ]; then
  cp .env.example .env
  echo "Создан .env. ОБЯЗАТЕЛЬНО отредактируй: nano .env — вставь свой DEEPSEEK_API_KEY"
else
  echo "Файл .env уже есть."
fi

echo "Устанавливаю зависимости (npm install)..."
npm install

echo ""
echo "Готово. Дальше:"
echo "  1. nano .env   — впиши DEEPSEEK_API_KEY=sk-твой_ключ"
echo "  2. npm start   — запуск сервера"
echo "  3. (опционально) ufw allow 3000 && ufw reload"
