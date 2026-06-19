import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Login from './components/Login';
import Register from './components/Register'; // 🆕 Import del componente Register
import './App.css';

const ProtectedRoute = ({ children }) => {
  const isAuth = localStorage.getItem('usuarioLogueado');
  if (!isAuth) return <Navigate to="/login" replace />;
  return children;
};

function MainApp() {
  const [mascotas, setMascotas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mostrarFormulario, setMostrarFormulario] = useState(false); 
  const [mascotaEncuentro, setMascotaEncuentro] = useState(null); 
  
  // ESTADO PARA LA IMAGEN: Guarda la URL temporal de la foto subida
  const [fotoEncuentro, setFotoEncuentro] = useState(null);
  
  const [filtroActivo, setFiltroActivo] = useState('PERDIDA');

  const [formulario, setFormulario] = useState({
    tipoReporte: 'PERDIDA',
    especie: '',
    raza: '',
    ubicacion: ''
  });
  
  const navigate = useNavigate();

  const cargarMascotas = async () => {
    try {
      setLoading(true);
      const res = await axios.get('/api/mascotas');
      setMascotas(res.data);
    } catch (error) {
      console.error("Error al conectar con el backend:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarMascotas();
  }, []);

  const handleChange = (e) => {
    setFormulario({ ...formulario, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('/api/mascotas', formulario);
      alert("✅ ¡Mascota registrada exitosamente!");
      setFormulario({ tipoReporte: 'PERDIDA', especie: '', raza: '', ubicacion: '' });
      setMostrarFormulario(false); 
      cargarMascotas();
    } catch (error) {
      alert("❌ Error al guardar la mascota.");
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  // FUNCIÓN PARA CAPTURAR LA IMAGEN: Crea la ruta en memoria para mostrarla al instante
  const handleImagenChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const imageUrl = URL.createObjectURL(file);
      setFotoEncuentro(imageUrl);
    }
  };

  const cerrarModalEncuentro = () => {
    setMascotaEncuentro(null);
    setFotoEncuentro(null); // Limpia la foto al cerrar
  };

  const handleConfirmarEncuentro = () => {
    alert(`¡Gracias por reportar a ${mascotaEncuentro.especie}! Notificaremos al dueño pronto.`);
    cerrarModalEncuentro();
  };

  const mascotasFiltradas = mascotas.filter(m => m.tipoReporte === filtroActivo);
  const imagenPlaceholder = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80";

  return (
    <div className="App">
      {/* BARRA DE NAVEGACIÓN */}
      <nav className="navbar">
        <div className="navbar-brand">
          <span className="logo-icon">🐾</span>
          <div className="logo-text">
            <strong>SANOS Y SALVOS</strong>
            <span>Red de Rescate 2026</span>
          </div>
        </div>
        <ul className="navbar-links">
          <li><a href="#voluntariado">VOLUNTARIADO</a></li>
          <li className="active"><a href="#perdidas">MASCOTAS PERDIDAS</a></li>
          <li><a href="#adopciones">ADOPCIONES</a></li>
        </ul>
        <div className="navbar-actions">
          <button className="btn-publicar" onClick={() => setMostrarFormulario(!mostrarFormulario)}>
            {mostrarFormulario ? "CANCELAR" : "+ PUBLICAR MASCOTA"}
          </button>
          <button className="btn-logout-nav" onClick={handleLogout}>Salir</button>
        </div>
      </nav>

      {/* HERO SECTION */}
      <header className="hero-section">
        <div className="hero-badge">SISTEMA DE BÚSQUEDA ACTIVO</div>
        <h1>MASCOTAS <span className="highlight">PERDIDAS</span><br/>EN TU SECTOR</h1>
        <p>Ayúdanos a reunir a las mascotas con sus familias. Revisa los reportes recientes.</p>
      </header>

      <main className="container">
        
        {/* MODAL 1: PUBLICAR NUEVA MASCOTA */}
        {mostrarFormulario && (
          <div className="form-modal-overlay" onClick={() => setMostrarFormulario(false)}>
            <form onSubmit={handleSubmit} className="form-card" onClick={(e) => e.stopPropagation()}>
              <h2>Registrar Nuevo Reporte</h2>
              <div className="form-group">
                <label>Estado del reporte:</label>
                <select name="tipoReporte" value={formulario.tipoReporte} onChange={handleChange}>
                  <option value="PERDIDA">Se busca (Perdida)</option>
                  <option value="ENCONTRADA">Encontrada (Busca familia)</option>
                  <option value="REUNIDO">Reunido con su familia</option>
                </select>
              </div>
              <input name="especie" placeholder="Especie y Nombre (Ej: Perro - Gaspar)" value={formulario.especie} onChange={handleChange} required />
              <input name="raza" placeholder="Raza o descripción" value={formulario.raza} onChange={handleChange} required />
              <input name="ubicacion" placeholder="Ubicación (Ej: Plaza de Puente Alto)" value={formulario.ubicacion} onChange={handleChange} required />
              <button type="submit" className="btn-submit">Publicar Reporte</button>
            </form>
          </div>
        )}

        {/* MODAL 2: REPORTAR ENCUENTRO DE UNA MASCOTA */}
        {mascotaEncuentro && (
          <div className="form-modal-overlay" onClick={cerrarModalEncuentro}>
            <div className="encuentro-card" onClick={(e) => e.stopPropagation()}>
              <button className="btn-close" onClick={cerrarModalEncuentro}>✕</button>
              
              <div className="encuentro-header">
                <img src={imagenPlaceholder} alt="Mascota" className="encuentro-img" />
                <div className="encuentro-info-header">
                  <span className="badge-encontrada">🎉 ¡MASCOTA ENCONTRADA!</span>
                  <h2>{mascotaEncuentro.especie}</h2>
                  <p>🦴 {mascotaEncuentro.raza} • 📍 {mascotaEncuentro.ubicacion}</p>
                </div>
              </div>

              <div className="encuentro-body">
                <h3>Completa los datos del hallazgo</h3>
                <p className="text-muted">Solo necesitamos algunos datos para verificar el encuentro.</p>

                {/* SECCIÓN DE SUBIDA DE IMAGEN */}
                <div className="upload-section">
                  <div className="upload-header">
                    <label>FOTO DE PRUEBA (OPCIONAL)</label>
                    <span className="upload-counter">{fotoEncuentro ? "1" : "0"} / 1</span>
                  </div>
                  <p className="upload-subtext">Sube una foto actual para verificar que es la misma mascota</p>
                  
                  {/* El label actúa como botón contenedor del input oculto */}
                  <label className="upload-box" style={{ padding: fotoEncuentro ? '0' : '20px', overflow: 'hidden', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <input 
                      type="file" 
                      accept="image/*" 
                      style={{ display: 'none' }} 
                      onChange={handleImagenChange}
                    />
                    {fotoEncuentro ? (
                      <img 
                        src={fotoEncuentro} 
                        alt="Vista previa del hallazgo" 
                        style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} 
                      />
                    ) : (
                      <span>📷 Agregar</span>
                    )}
                  </label>
                </div>

                <div className="form-group">
                  <label>¿Dónde lo encontraste? *</label>
                  <textarea placeholder="Ej: Lo encontré en la plaza cerca de mi casa..."></textarea>
                </div>

                <h3 className="section-title">Tu información de contacto</h3>
                <div className="form-group">
                  <label>Tu nombre *</label>
                  <input type="text" placeholder="Ej: María González" />
                </div>
                <div className="form-group">
                  <label>Teléfono / WhatsApp *</label>
                  <input type="text" placeholder="+56 9 1234 5678" />
                </div>

                <div className="owner-contact">
                  <p className="owner-title">📞 Contacto del dueño:</p>
                  <p className="owner-phone">950071932</p>
                </div>
              </div>

              <div className="encuentro-footer">
                <button className="btn-cancelar" onClick={cerrarModalEncuentro}>Cancelar</button>
                <button className="btn-confirmar" onClick={handleConfirmarEncuentro}>Confirmar encuentro</button>
              </div>
            </div>
          </div>
        )}

        {/* TABS DE FILTRADO PRINCIPALES */}
        <div className="filtros-bar">
          <div className={`filtro-tab ${filtroActivo === 'PERDIDA' ? 'active' : ''}`} onClick={() => setFiltroActivo('PERDIDA')}>
            🔍 Perdidos
          </div>
          <div className={`filtro-tab ${filtroActivo === 'ENCONTRADA' ? 'active' : ''}`} onClick={() => setFiltroActivo('ENCONTRADA')}>
            🧡 Buscan a su Familia
          </div>
          <div className={`filtro-tab ${filtroActivo === 'REUNIDO' ? 'active' : ''}`} onClick={() => setFiltroActivo('REUNIDO')}>
            🐾 Reunidos
          </div>
        </div>

        {/* FILTROS AVANZADOS */}
        <div className="advanced-filters">
          <div className="search-row">
            <label>Buscar mascotas</label>
            <div className="search-input-wrapper">
              <span>🔍</span>
              <input type="text" placeholder="Nombre, raza, ubicación, descripción..." />
            </div>
          </div>
          
          <div className="dropdowns-row">
            <div className="filter-group">
              <label>Especie</label>
              <select><option>Todas ({mascotasFiltradas.length})</option><option>Perro</option><option>Gato</option></select>
            </div>
            <div className="filter-group">
              <label>Raza</label>
              <select><option>Todas</option></select>
            </div>
            <div className="filter-group">
              <label>Tamaño</label>
              <select><option>Todos</option></select>
            </div>
            <div className="filter-group">
              <label>Color</label>
              <select><option>Todos</option></select>
            </div>
          </div>

          <div className="dropdowns-row" style={{ marginTop: '15px' }}>
            <div className="filter-group" style={{ maxWidth: '25%' }}>
              <label>Comuna</label>
              <select>
                <option>Todas</option>
                <option>Puente Alto</option>
                <option>San Joaquín</option>
                <option>La Florida</option>
                <option>Pirque</option>
              </select>
            </div>
          </div>
        </div>

        {/* LISTADO DE TARJETAS */}
        <section className="listado">
          <div className="listado-header">
            <h3>{filtroActivo === 'REUNIDO' ? 'Historias felices' : 'Últimos reportes'}</h3>
            <span>{mascotasFiltradas.length} {filtroActivo === 'REUNIDO' ? 'mascotas reunidas con su familia' : 'resultados'}</span>
          </div>

          {loading ? (
            <p className="loading-text">Cargando base de datos...</p>
          ) : (
            <div className="mascotas-grid">
              {mascotasFiltradas.length === 0 && <p>No hay reportes en esta categoría por ahora.</p>}
              
              {mascotasFiltradas.map(m => {
                const esReunido = m.tipoReporte === 'REUNIDO';

                return (
                  <div key={m.id} className={`mascota-card ${esReunido ? 'is-reunido' : ''}`}>
                    <div className="card-image-container">
                      <img src={imagenPlaceholder} alt="Mascota" className="card-image" />
                      
                      {esReunido ? (
                        <>
                          <span className="badge-reunido-top">REUNIDO CON SU DUEÑO</span>
                          <div className="stamp-reunido">REUNIDO</div>
                        </>
                      ) : (
                        <span className={`card-badge status-${m.tipoReporte}`}>{m.tipoReporte}</span>
                      )}
                    </div>
                    
                    <div className="card-content">
                      <h3 className={`mascota-titulo ${esReunido ? 'reunido-text' : ''}`}>{m.especie}</h3>
                      <p className="mascota-raza">{m.raza}</p>
                      <div className="mascota-ubicacion">
                        📍 <span>{m.ubicacion}</span>
                      </div>
                    </div>
                    
                    {!esReunido && (
                      <div className="card-footer">
                        <button className="btn-reportar" onClick={() => setMascotaEncuentro(m)}>
                          REPORTAR ENCUENTRO
                        </button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </section>
      </main>

      {/* FOOTER */}
      <footer className="footer">
        <div className="footer-content">
          <div className="footer-column">
            <div className="footer-brand">
              <span className="logo-icon">🐾</span>
              <div className="logo-text">
                <strong>SANOS Y SALVOS</strong>
                <span>Red de Rescate 2026</span>
              </div>
            </div>
            <p className="footer-description">
              Plataforma dedicada a la búsqueda y rescate de mascotas en la comuna de Puente Alto y sus alrededores.
            </p>
            <div className="footer-illustration">🤝🐾</div>
          </div>

          <div className="footer-column text-center">
            <h4 className="footer-title">EQUIPO DESARROLLADOR</h4>
            <p className="footer-subtitle">Desarrollado con ❤️ por</p>
            <ul className="developer-list">
              <li>Bastián San Martín</li>
              <li>Cristóbal Ibarra</li>
              <li>César Flores</li>
            </ul>
            <p className="footer-copyright">PARA AYUDAR A LA COMUNIDAD • © 2026</p>
          </div>

          <div className="footer-column">
            <h4 className="footer-title">EN PRENSA</h4>
            <div className="prensa-tags">
              <span>Meganoticias</span>
              <span>Canal 13</span>
              <span>ADN Radio</span>
              <span>Puente Alto Al Día</span>
              <span>Biobio</span>
              <span>CHV Noticias</span>
              <span>24 Horas</span>
            </div>
          </div>
        </div>

        <div className="footer-divider"></div>

        <div className="footer-bottom">
          <button className="btn-footer-contacto">✉️ Contacto</button>
          <button className="btn-footer-acopio">🔥 Puntos de Acopio ↗</button>
        </div>
      </footer>
    </div>
  );
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} /> {/* 🆕 Ruta de registro */}
        <Route path="/" element={<ProtectedRoute><MainApp /></ProtectedRoute>} />
      </Routes>
    </Router>
  );
}

export default App;