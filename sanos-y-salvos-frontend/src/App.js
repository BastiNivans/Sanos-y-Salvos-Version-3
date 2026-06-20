import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents, useMap } from 'react-leaflet';
import { OpenStreetMapProvider, GeoSearchControl } from 'leaflet-geosearch';
import 'leaflet/dist/leaflet.css';
import 'leaflet-geosearch/dist/geosearch.css';
import L from 'leaflet';
import './App.css';

// Configuración obligatoria para los iconos por defecto de Leaflet en React
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// 🔍 COMPONENTE BUSCADOR: Añade la barra de búsqueda de direcciones dentro del mapa
function BuscadorMapa({ alCambiarUbicacion }) {
  const map = useMap();

  useEffect(() => {
    const provider = new OpenStreetMapProvider({
      params: {
        'accept-language': 'es',
        countrycodes: 'cl', // Limita las búsquedas estrictamente a Chile
      },
    });

    const searchControl = new GeoSearchControl({
      provider: provider,
      style: 'bar', 
      showMarker: false, 
      showPopup: false,
      autoClose: true,
      retainZoomLevel: false,
      animateZoom: true,
      searchLabel: 'Ingresa un lugar (Ej: Plaza de Puente Alto)',
    });

    map.addControl(searchControl);

    map.on('geosearch/showlocation', (result) => {
      const lat = result.location.y;
      const lng = result.location.x;
      alCambiarUbicacion([lat, lng]);
    });

    return () => map.removeControl(searchControl);
  }, [map, alCambiarUbicacion]);

  return null;
}

function App() {
  const [mascotas, setMascotas] = useState([]);
  const [coincidenciasCercanas, setCoincidenciasCercanas] = useState([]); // 📡 Estado para almacenar coincidencias de PostGIS
  const [especie, setEspecie] = useState('');
  const [raza, setRaza] = useState('');
  const [tipoReporte, setTipoReporte] = useState('Perdida');
  const [posicionSeleccionada, setPosicionSeleccionada] = useState([-33.6167, -70.5750]);

  // 🛰️ CONSULTA AL MICROSERVICIO DE COINCIDENCIAS (Puerto 8082)
  const buscarCoincidenciasPostGIS = async (lat, lng, esp, tipo) => {
    try {
      const url = `http://localhost:8082/api/coincidencias/buscar?lat=${lat}&lng=${lng}&especie=${esp}&tipoReporte=${tipo}`;
      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        setCoincidenciasCercanas(data);
      }
    } catch (error) {
      console.error("Error al conectar con coincidencias:", error);
    }
  };

  // Ejecuta la búsqueda automática cuando el usuario interactúa o cambia el formulario
  useEffect(() => {
    if (especie) {
      buscarCoincidenciasPostGIS(posicionSeleccionada[0], posicionSeleccionada[1], especie, tipoReporte);
    }
  }, [posicionSeleccionada, especie, tipoReporte]);

  const obtenerMascotas = async () => {
    try {
      const response = await fetch('http://localhost:8081/api/mascotas');
      if (response.ok) {
        const data = await response.json();
        setMascotas(data);
      }
    } catch (error) {
      console.error("Error al obtener mascotas:", error);
    }
  };

  useEffect(() => {
    obtenerMascotas();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const nuevaMascotaDTO = {
      especie: especie,
      raza: raza,
      tipoReporte: tipoReporte,
      ubicacion: `${posicionSeleccionada[0]},${posicionSeleccionada[1]}`
    };

    try {
      const response = await fetch('http://localhost:8081/api/mascotas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(nuevaMascotaDTO),
      });

      if (response.ok) {
        alert('¡Mascota publicada con éxito! 🎉');
        setEspecie('');
        setRaza('');
        obtenerMascotas();
      } else {
        alert('❌ Error en el servidor al guardar.');
      }
    } catch (error) {
      console.error(error);
      alert('❌ Error al conectar con el backend.');
    }
  };

  function ManejadorClickMapa() {
    useMapEvents({
      click(e) {
        setPosicionSeleccionada([e.latlng.lat, e.latlng.lng]);
      },
    });
    return posicionSeleccionada ? <Marker position={posicionSeleccionada} /> : null;
  }

  return (
    <div className="dashboard-container">
      <header className="app-header">
        <h1>Sanos y Salvos 🐾</h1>
        <p>Plataforma Geoespacial de Recuperación de Mascotas</p>
      </header>

      <div className="main-content">
        
        {/* COLUMNA IZQUIERDA: FORMULARIO */}
        <aside className="form-section">
          <h2>Registrar Reporte</h2>
          <form onSubmit={handleSubmit} className="modern-form">
            <div className="form-group">
              <label>Estado:</label>
              <select value={tipoReporte} onChange={(e) => setTipoReporte(e.target.value)}>
                <option value="Perdida">Perdida</option>
                <option value="Encontrada">Encontrada</option>
              </select>
            </div>

            <div className="form-group">
              <label>Especie:</label>
              <input type="text" placeholder="Ej: Perro" value={especie} onChange={(e) => setEspecie(e.target.value)} required />
            </div>

            <div className="form-group">
              <label>Raza:</label>
              <input type="text" placeholder="Ej: Pug" value={raza} onChange={(e) => setRaza(e.target.value)} required />
            </div>

            <div className="form-group">
              <label>Ubicación del reporte:</label>
              <div className="geo-indicator">
                📍 {posicionSeleccionada[0].toFixed(4)}, {posicionSeleccionada[1].toFixed(4)}
                <small style={{display:'block', color:'#e67e22', marginTop:'4px'}}>
                  (Escribe un lugar en el buscador o haz clic en el mapa)
                </small>
              </div>
            </div>

            <button type="submit" className="btn-submit">Publicar Reporte</button>
          </form>
        </aside>

        {/* COLUMNA DERECHA: MAPA INTERACTIVO + LISTA */}
        <main className="visualization-section">
          
          <div className="map-wrapper">
            <MapContainer center={[-33.6167, -70.5750]} zoom={13} style={{ height: '380px', width: '100%' }}>
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; OpenStreetMap'
              />
              
              <BuscadorMapa alCambiarUbicacion={setPosicionSeleccionada} />
              <ManejadorClickMapa />

              {/* 1. Dibujar pines generales de la base de datos */}
              {mascotas.map((pet) => {
                if (!pet.ubicacion || !pet.ubicacion.includes(',')) return null;
                const [lat, lng] = pet.ubicacion.split(',').map(Number);
                if (isNaN(lat) || isNaN(lng)) return null;
                return (
                  <Marker key={`pet-${pet.id}`} position={[lat, lng]}>
                    <Popup>
                      <strong>{pet.especie}</strong> - {pet.raza}<br/>
                      Estado: <span style={{color: pet.tipoReporte === 'Perdida' ? 'red' : 'green'}}>{pet.tipoReporte}</span>
                    </Popup>
                  </Marker>
                );
              })}

              {/* 2. Dibujar pines de Coincidencias Cercanas (PostGIS a < 3km) */}
              {coincidenciasCercanas.map((match) => {
                if (!match.ubicacion) return null;
                let lat, lng;
                if (match.ubicacion.includes('POINT')) {
                  const coords = match.ubicacion.replace('POINT(', '').replace(')', '').split(' ');
                  lng = Number(coords[0]);
                  lat = Number(coords[1]);
                } else {
                  [lat, lng] = match.ubicacion.split(',').map(Number);
                }
                if (isNaN(lat) || isNaN(lng)) return null;

                return (
                  <Marker key={`match-${match.id}`} position={[lat, lng]}>
                    <Popup>
                      <div style={{textAlign: 'center'}}>
                        <span style={{background: '#e67e22', color: 'white', padding: '2px 5px', borderRadius: '3px', fontSize: '10px', fontWeight: 'bold'}}>
                          🔥 COINCIDENCIA CERCANA
                        </span><br/>
                        <strong>{match.especie}</strong> - {match.raza}<br/>
                        <small>¡A menos de 3km del punto!</small>
                      </div>
                    </Popup>
                  </Marker>
                );
              })}
            </MapContainer>
          </div>

          <h2>Reportes Recientes</h2>
          {mascotas.length === 0 ? (
            <p>No hay reportes registrados aún.</p>
          ) : (
            <div className="cards-grid">
              {mascotas.map((pet) => (
                <div key={pet.id} className="pet-card">
                  <span className={`badge ${pet.tipoReporte.toLowerCase()}`}>{pet.tipoReporte}</span>
                  <h3>{pet.especie}</h3>
                  <p className="raza-txt">{pet.raza}</p>
                  <p className="coords-txt">📍 {pet.ubicacion}</p>
                </div>
              ))}
            </div>
          )}
        </main>

      </div>
    </div>
  );
}

export default App;