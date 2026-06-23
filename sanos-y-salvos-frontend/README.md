# Frontend — Sanos y Salvos

Aplicación web (React) para reportar mascotas perdidas/encontradas, ver coincidencias y visualizar el mapa de reportes.

## Tecnologías

- React 19 (Create React App / `react-scripts`)
- React Router DOM
- React Leaflet (mapa)
- Axios / Fetch API
- Testing Library (Jest + React Testing Library)

## Requisitos previos

- Node.js y npm instalados.
- El `bff` corriendo en `http://localhost:8080` (el frontend hace `proxy` hacia ese puerto, ver `package.json`).

## Instalar

```bash
cd sanos-y-salvos-frontend
npm install
```

## Ejecutar en desarrollo

```bash
npm start
```

Abre automáticamente `http://localhost:3000`. Necesitas el `bff` (y por extensión `mascotas`, `coincidencias`, `geolocalizacion`) corriendo para que el login, el registro y los reportes funcionen.

## Ejecutar las pruebas

```bash
npm test
```

Esto corre Jest en modo interactivo (watch). Para una sola corrida con reporte de cobertura:

```bash
npm test -- --coverage --watchAll=false
```

El reporte de cobertura queda en `coverage/lcov-report/index.html`.

## Construir para producción

```bash
npm run build
```

Genera la versión optimizada en la carpeta `build/`.

## Estructura principal

```
src/
  components/   # Páginas y componentes (Login, Register, mapa, formularios, etc.)
  App.js        # Rutas de la aplicación
```
