import { Router, Request, Response } from 'express';
import path from 'path';
import fs from 'fs';

export const swaggerRouter = Router();

// Endpoint to fetch raw OpenAPI spec
swaggerRouter.get('/openapi.yaml', (_req: Request, res: Response) => {
  const specPath = path.resolve(__dirname, '../../specs/openapi.yaml');
  if (fs.existsSync(specPath)) {
    res.setHeader('Content-Type', 'text/yaml; charset=utf-8');
    return res.sendFile(specPath);
  }
  return res.status(404).send('OpenAPI specification not found');
});

// Interactive Swagger UI HTML Page
swaggerRouter.get('/swagger', (_req: Request, res: Response) => {
  const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Omni-Protocol Platform | Swagger UI</title>
  <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
  <style>
    body { margin: 0; background: #0b0f19; font-family: system-ui, sans-serif; }
    .topbar { display: none !important; }
    .swagger-ui {
      filter: invert(88%) hue-rotate(180deg);
      max-width: 1200px;
      margin: 0 auto;
      padding: 20px;
    }
  </style>
</head>
<body>
  <div id="swagger-ui"></div>
  <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
  <script>
    window.onload = () => {
      SwaggerUIBundle({
        url: '/docs/openapi.yaml',
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIBundle.SwaggerUIStandalonePreset
        ],
        layout: "BaseLayout"
      });
    };
  </script>
</body>
</html>`;

  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  return res.send(html);
});
