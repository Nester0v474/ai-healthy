const express = require('express');
const cors = require('cors');
const axios = require('axios');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
    if (req.body && Object.keys(req.body).length > 0) {
        const bodyPreview = JSON.stringify(req.body).substring(0, 200);
        console.log('Body preview:', bodyPreview + (JSON.stringify(req.body).length > 200 ? '...' : ''));
    }
    next();
});

const DEEPSEEK_API_KEY = process.env.DEEPSEEK_API_KEY;
if (!DEEPSEEK_API_KEY) {
    console.error('ERROR: DEEPSEEK_API_KEY not set in .env file. Create .env with DEEPSEEK_API_KEY=your_key');
    process.exit(1);
}
const DEEPSEEK_API_URL = 'https://api.deepseek.com/v1/chat/completions';

app.post('/api/chat', async (req, res) => {
    try {
        const { model, messages, temperature, max_tokens, stream } = req.body;

        if (!messages || !Array.isArray(messages) || messages.length === 0) {
            return res.status(400).json({
                error: {
                    message: 'Invalid request: messages is required',
                    type: 'invalid_request_error',
                    code: 'invalid_parameter'
                }
            });
        }

        const requestBody = {
            model: model || 'deepseek-chat',
            messages: messages,
            temperature: temperature || 0.7,
            max_tokens: max_tokens || 2000,
            stream: stream || false
        };

        console.log(`Making request to DeepSeek API: ${DEEPSEEK_API_URL}`);

        const response = await axios.post(
            DEEPSEEK_API_URL,
            requestBody,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${DEEPSEEK_API_KEY}`
                },
                timeout: 60000
            }
        );

        res.json(response.data);
    } catch (error) {
        console.error('DeepSeek API Error:', error.message);
        console.error('Error stack:', error.stack);

        if (error.response) {
            console.error('DeepSeek API Response Error:', {
                status: error.response.status,
                statusText: error.response.statusText,
                data: error.response.data
            });
            res.status(error.response.status || 500).json({
                error: {
                    message: error.response.data?.error?.message || error.message,
                    type: error.response.data?.error?.type || 'api_error',
                    code: error.response.data?.error?.code || 'unknown_error'
                }
            });
        } else if (error.request) {
            console.error('No response received from DeepSeek API');
            res.status(500).json({
                error: {
                    message: 'Network error: No response from DeepSeek API',
                    type: 'network_error',
                    code: 'network_error'
                }
            });
        } else {
            console.error('Request setup error:', error.message);
            res.status(500).json({
                error: {
                    message: error.message || 'Internal server error',
                    type: 'internal_error',
                    code: 'internal_error'
                }
            });
        }
    }
});

app.get('/health', (req, res) => {
    res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server is running on port ${PORT}`);
    console.log(`Server listening on: http://0.0.0.0:${PORT}`);
    if (process.env.NODE_ENV !== 'production') {
        console.log(`Local access: http://localhost:${PORT}`);
        console.log(`Network access: http://YOUR_LOCAL_IP:${PORT}`);
    }
    console.log(`DeepSeek API Key: ${DEEPSEEK_API_KEY.substring(0, 10)}...`);
    console.log(`Environment: ${process.env.NODE_ENV || 'development'}`);
});
