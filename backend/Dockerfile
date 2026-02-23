# Я собираю образ для деплоя на Docker
FROM node:18-alpine

WORKDIR /app

# Копирую зависимости
COPY package*.json ./
RUN npm ci --only=production

# Копирую мой код сервера
COPY . .

EXPOSE 3000
CMD ["node", "server.js"]

