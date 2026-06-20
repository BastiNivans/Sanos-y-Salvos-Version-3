import React, { useState, useEffect, useMemo } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Login from './components/Login';
import Register from './components/Register';
import './App.css';

// 🗺️ IMPORTS PARA OPENSTREETMAP
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

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
  
  const [coincidencias, setCoincidencias] = useState([]);
  const [mascotaCoincidencias, setMascotaCoincidencias] = useState(null);
  const [mostrarModalCoincidencias, setMostrarModalCoincidencias] = useState(false);
  const [cargandoCoincidencias, setCargandoCoincidencias] = useState(false);
  
  const [mascotaSeleccionada, setMascotaSeleccionada] = useState(null);
  const [imagenActiva, setImagenActiva] = useState(0);
  
  const [fotoEncuentro, setFotoEncuentro] = useState(null);
  const [filtroActivo, setFiltroActivo] = useState('PERDIDA');

  // Estados para búsqueda y filtros
  const [textoBusqueda, setTextoBusqueda] = useState('');
  const [filtroEspecie, setFiltroEspecie] = useState('TODAS');
  const [filtroRaza, setFiltroRaza] = useState('TODAS');

  // Estados para menú de usuario
  const [mostrarMenuUsuario, setMostrarMenuUsuario] = useState(false);
  const [usuarioEmail, setUsuarioEmail] = useState('');

  // 🗺️ ESTADOS PARA EL MAPA
  const [mostrarMapa, setMostrarMapa] = useState(false);
  const [mascotasParaMapa, setMascotasParaMapa] = useState([]);

  // 🆕 ESTADOS PARA AUTOCOMPLETADO DE UBICACIÓN
  const [sugerenciasUbicacion, setSugerenciasUbicacion] = useState([]);
  const [mostrarSugerencias, setMostrarSugerencias] = useState(false);

  const [formulario, setFormulario] = useState({
    tipoReporte: 'PERDIDA',
    especie: '',
    raza: '',
    ubicacion: ''
  });
  
  const [imagenesMascota, setImagenesMascota] = useState([]);
  const [previewUrls, setPreviewUrls] = useState([]);
  
  const navigate = useNavigate();

  // Obtener email del usuario al cargar
  useEffect(() => {
    const emailGuardado = localStorage.getItem('usuarioEmail') || localStorage.getItem('correo');
    if (emailGuardado) {
      setUsuarioEmail(emailGuardado);
    }
  }, []);

  // Cerrar menú al hacer clic fuera
  useEffect(() => {
    const handleClickOutside = () => {
      if (mostrarMenuUsuario) {
        setMostrarMenuUsuario(false);
      }
    };

    if (mostrarMenuUsuario) {
      document.addEventListener('click', handleClickOutside);
    }

    return () => {
      document.removeEventListener('click', handleClickOutside);
    };
  }, [mostrarMenuUsuario]);

  // 🆕 EFECTO PARA AUTOCOMPLETADO (espera 500ms antes de buscar)
  useEffect(() => {
    if (formulario.ubicacion.length >= 3) {
      const timer = setTimeout(() => {
        buscarSugerencias(formulario.ubicacion);
      }, 500); // Espera medio segundo antes de buscar

      return () => clearTimeout(timer);
    } else {
      setSugerenciasUbicacion([]);
      setMostrarSugerencias(false);
    }
  }, [formulario.ubicacion]);

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

  const cargarCoincidencias = async () => {
    try {
      const res = await axios.get('/api/coincidencias');
      setCoincidencias(res.data);
    } catch (error) {
      console.error("Error al cargar coincidencias:", error);
    }
  };

  useEffect(() => {
    cargarMascotas();
    cargarCoincidencias();
  }, []);

  const handleChange = (e) => {
    setFormulario({ ...formulario, [e.target.name]: e.target.value });
  };

  const handleImagenesChange = (e) => {
    const files = Array.from(e.target.files);
    setImagenesMascota(files);
    const urls = files.map(file => URL.createObjectURL(file));
    setPreviewUrls(urls);
  };

  const removeImagen = (index) => {
    const nuevasImagenes = imagenesMascota.filter((_, i) => i !== index);
    const nuevosUrls = previewUrls.filter((_, i) => i !== index);
    setImagenesMascota(nuevasImagenes);
    setPreviewUrls(nuevosUrls);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const convertirABase64 = (file) => {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = (error) => reject(error);
      });
    };

    try {
      const imagenesBase64 = [];
      for (const imagen of imagenesMascota) {
        const base64 = await convertirABase64(imagen);
        imagenesBase64.push(base64);
      }

      const datosMascota = {
        especie: formulario.especie,
        raza: formulario.raza,
        tipoReporte: formulario.tipoReporte,
        ubicacion: formulario.ubicacion,
        imagenesBase64: imagenesBase64
      };

      await axios.post('/api/mascotas', datosMascota);
      
      alert("✅ ¡Mascota registrada exitosamente! El sistema buscará coincidencias automáticamente.");
      setFormulario({ tipoReporte: 'PERDIDA', especie: '', raza: '', ubicacion: '' });
      setImagenesMascota([]);
      setPreviewUrls([]);
      setMostrarFormulario(false); 
      cargarMascotas();
      cargarCoincidencias();
    } catch (error) {
      console.error("Error al guardar:", error);
      alert("❌ Error al guardar la mascota.");
    }
  };

  const buscarCoincidencias = async (mascota) => {
    setCargandoCoincidencias(true);
    setMascotaCoincidencias(mascota);
    setMostrarModalCoincidencias(true);
    
    try {
      const response = await axios.get(`/api/coincidencias/calcular/${mascota.id}`);
      console.log("✅ Coincidencias calculadas:", response.data);
      await cargarCoincidencias();
    } catch (error) {
      console.error("❌ Error al calcular coincidencias:", error);
    } finally {
      setCargandoCoincidencias(false);
    }
  };

  const obtenerMascotaPorId = (id) => {
    return mascotas.find(m => m.id === id);
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const handleImagenChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const imageUrl = URL.createObjectURL(file);
      setFotoEncuentro(imageUrl);
    }
  };

  const cerrarModalEncuentro = () => {
    setMascotaEncuentro(null);
    setFotoEncuentro(null);
  };

  const handleConfirmarEncuentro = () => {
    alert(`¡Gracias por reportar a ${mascotaEncuentro.especie}! Notificaremos al dueño pronto.`);
    cerrarModalEncuentro();
  };

  const abrirDetalleMascota = (mascota) => {
    setMascotaSeleccionada(mascota);
    setImagenActiva(0);
  };

  const cerrarDetalleMascota = () => {
    setMascotaSeleccionada(null);
    setImagenActiva(0);
  };

  const obtenerListaImagenes = (imagenesUrls) => {
    if (!imagenesUrls || imagenesUrls.trim() === '') return [];
    return imagenesUrls.split(',').map(img => img.trim()).filter(img => img !== '');
  };

  const obtenerUrlImagen = (imagenesUrls) => {
    if (!imagenesUrls || imagenesUrls.trim() === '') return "https://images.unsplash.com/photo-1543466835-00a7907e9de1?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80";
    const imagenes = imagenesUrls.split(',');
    if (imagenes.length === 0 || !imagenes[0]) return "https://images.unsplash.com/photo-1543466835-00a7907e9de1?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80";
    return `http://localhost:8081${imagenes[0].trim()}`;
  };

  const contarImagenes = (imagenesUrls) => {
    if (!imagenesUrls || imagenesUrls.trim() === '') return 0;
    return imagenesUrls.split(',').filter(img => img.trim() !== '').length;
  };

  const obtenerCoincidenciasDeMascota = (mascotaId) => {
    return coincidencias.filter(c => 
      c.idMascotaPerdida === mascotaId || c.idMascotaEncontrada === mascotaId
    );
  };

  // 🗺️ FUNCIÓN PARA CARGAR MASCOTAS EN EL MAPA
  const cargarMascotasEnMapa = async () => {
    try {
      const response = await axios.get('http://localhost:8083/api/geolocalizacion/todas');
      setMascotasParaMapa(response.data);
      setMostrarMapa(true);
    } catch (error) {
      console.error("Error al cargar el mapa:", error);
      alert("❌ Error al cargar el mapa. Asegúrate de que el microservicio de geolocalización (puerto 8083) esté corriendo.");
    }
  };

  // 🆕 FUNCIÓN PARA BUSCAR DIRECCIONES REALES EN OPENSTREETMAP
  const buscarSugerencias = async (texto) => {
    if (texto.length < 3) {
      setSugerenciasUbicacion([]);
      return;
    }

    try {
      // API gratuita de OpenStreetMap (Nominatim)
      // countrycodes=cl limita la búsqueda a Chile
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(texto)}&countrycodes=cl&limit=5`
      );
      const data = await response.json();
      
      const sugerencias = data.map(item => item.display_name);
      setSugerenciasUbicacion(sugerencias);
      setMostrarSugerencias(sugerencias.length > 0);
    } catch (error) {
      console.error("Error al buscar ubicación:", error);
    }
  };

  // 🆕 FUNCIÓN PARA SELECCIONAR UNA SUGERENCIA
  const seleccionarSugerencia = (direccion) => {
    setFormulario({ ...formulario, ubicacion: direccion });
    setMostrarSugerencias(false);
    setSugerenciasUbicacion([]);
  };

  // Opciones dinámicas para los filtros
  const opcionesEspecie = useMemo(() => {
    const especies = [...new Set(mascotas.map(m => {
      if (!m.especie) return null;
      const parte = m.especie.split('-')[0].trim();
      return parte;
    }).filter(e => e))];
    return especies.sort();
  }, [mascotas]);

  const opcionesRaza = useMemo(() => {
    const razas = [...new Set(mascotas.map(m => m.raza).filter(r => r && r.trim() !== ''))];
    return razas.sort();
  }, [mascotas]);

  // Filtrado combinado
  const mascotasFiltradas = useMemo(() => {
    return mascotas.filter(m => {
      if (m.tipoReporte !== filtroActivo) return false;
      
      if (textoBusqueda.trim() !== '') {
        const busqueda = textoBusqueda.toLowerCase();
        const textoMascota = `${m.especie || ''} ${m.raza || ''} ${m.ubicacion || ''}`.toLowerCase();
        if (!textoMascota.includes(busqueda)) return false;
      }
      
      if (filtroEspecie !== 'TODAS') {
        const especieMascota = (m.especie || '').split('-')[0].trim().toLowerCase();
        if (especieMascota !== filtroEspecie.toLowerCase()) return false;
      }
      
      if (filtroRaza !== 'TODAS') {
        if ((m.raza || '').toLowerCase() !== filtroRaza.toLowerCase()) return false;
      }
      
      return true;
    });
  }, [mascotas, filtroActivo, textoBusqueda, filtroEspecie, filtroRaza]);

  const limpiarFiltros = () => {
    setTextoBusqueda('');
    setFiltroEspecie('TODAS');
    setFiltroRaza('TODAS');
  };

  const imagenPlaceholder = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80";

  return (
    <div className="App">
      {/* NAVBAR CON MENÚ DE USUARIO */}
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
          
          {/* 🗺️ BOTÓN PARA VER MAPA */}
          <button 
            onClick={cargarMascotasEnMapa}
            style={{
              background: '#10b981',
              color: 'white',
              border: 'none',
              padding: '10px 20px',
              borderRadius: '6px',
              fontWeight: 'bold',
              cursor: 'pointer',
              marginLeft: '10px'
            }}
          >
            🗺️ Ver Mapa
          </button>
          
          {/* MENÚ DE USUARIO */}
          <div className="usuario-menu-container" onClick={(e) => e.stopPropagation()}>
            <button 
              className="btn-usuario" 
              onClick={() => setMostrarMenuUsuario(!mostrarMenuUsuario)}
            >
              <span className="usuario-icon">👤</span>
              <div className="usuario-info">
                <div className="usuario-bienvenida">Bienvenido</div>
                <div className="usuario-nombre">
                  {usuarioEmail ? usuarioEmail.split('@')[0] : 'Usuario'}
                </div>
              </div>
              <span className="usuario-flecha">{mostrarMenuUsuario ? '▲' : '▼'}</span>
            </button>
            
            {mostrarMenuUsuario && (
              <div className="usuario-dropdown-menu">
                <div className="usuario-dropdown-header">
                  <div className="usuario-dropdown-label">Sesión iniciada como</div>
                  <div className="usuario-dropdown-email">{usuarioEmail}</div>
                </div>
                <div className="usuario-dropdown-body">
                  <button 
                    className="btn-cerrar-sesion"
                    onClick={() => {
                      handleLogout();
                      setMostrarMenuUsuario(false);
                    }}
                  >
                    <span>🚪</span>
                    Cerrar sesión
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </nav>

      {/* HERO SECTION */}
      <header className="hero-section">
        <div className="hero-badge">SISTEMA DE BÚSQUEDA ACTIVO</div>
        <h1>MASCOTAS <span className="highlight">PERDIDAS</span><br/>EN TU SECTOR</h1>
        <p>Ayúdanos a reunir a las mascotas con sus familias. Revisa los reportes recientes.</p>
      </header>

      <main className="container">
        
        {/* MODAL 1: FORMULARIO DE REGISTRO */}
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
              
              {/* 🆕 INPUT DE UBICACIÓN CON AUTOCOMPLETADO */}
              <div className="form-group" style={{ position: 'relative' }}>
                <label>Ubicación (Escribe para buscar)</label>
                <input 
                  name="ubicacion" 
                  placeholder="Ej: Plaza de Puente Alto..." 
                  value={formulario.ubicacion} 
                  onChange={(e) => {
                    setFormulario({ ...formulario, ubicacion: e.target.value });
                    setMostrarSugerencias(true);
                  }}
                  onBlur={() => setTimeout(() => setMostrarSugerencias(false), 200)}
                  required 
                />
                
                {/* 🗺️ LISTA DESPLEGABLE DE SUGERENCIAS */}
                {mostrarSugerencias && sugerenciasUbicacion.length > 0 && (
                  <ul style={{
                    position: 'absolute',
                    top: '100%',
                    left: 0,
                    right: 0,
                    background: 'white',
                    border: '1px solid #cbd5e1',
                    borderRadius: '8px',
                    marginTop: '5px',
                    maxHeight: '200px',
                    overflowY: 'auto',
                    listStyle: 'none',
                    padding: 0,
                    margin: 0,
                    zIndex: 1000,
                    boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)'
                  }}>
                    {sugerenciasUbicacion.map((sugerencia, index) => (
                      <li 
                        key={index}
                        onClick={() => seleccionarSugerencia(sugerencia)}
                        style={{
                          padding: '10px 15px',
                          cursor: 'pointer',
                          borderBottom: index < sugerenciasUbicacion.length - 1 ? '1px solid #f1f5f9' : 'none',
                          fontSize: '0.9rem',
                          color: '#334155'
                        }}
                        onMouseEnter={(e) => e.target.style.background = '#f8fafc'}
                        onMouseLeave={(e) => e.target.style.background = 'white'}
                      >
                        📍 {sugerencia}
                      </li>
                    ))}
                  </ul>
                )}
                
                {mostrarSugerencias && formulario.ubicacion.length >= 3 && sugerenciasUbicacion.length === 0 && (
                  <p style={{ color: '#ef4444', fontSize: '0.85rem', marginTop: '5px' }}>
                    ⚠️ No se encontraron direcciones reales con ese texto.
                  </p>
                )}
              </div>
              
              <div className="form-group">
                <label>Fotos de la mascota (opcional):</label>
                <input type="file" accept="image/*" multiple onChange={handleImagenesChange} style={{ marginTop: '8px' }} />
                <small style={{ color: '#64748b', fontSize: '0.85rem' }}>Puedes seleccionar múltiples imágenes</small>
              </div>
              {previewUrls.length > 0 && (
                <div className="imagenes-preview" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(80px, 1fr))', gap: '10px', marginTop: '10px' }}>
                  {previewUrls.map((url, index) => (
                    <div key={index} style={{ position: 'relative' }}>
                      <img src={url} alt={`Preview ${index + 1}`} style={{ width: '100%', height: '80px', objectFit: 'cover', borderRadius: '8px', border: '1px solid #cbd5e1' }} />
                      <button type="button" onClick={() => removeImagen(index)} style={{ position: 'absolute', top: '-8px', right: '-8px', background: '#ef4444', color: 'white', border: 'none', borderRadius: '50%', width: '24px', height: '24px', cursor: 'pointer', fontSize: '14px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>×</button>
                    </div>
                  ))}
                </div>
              )}
              <button type="submit" className="btn-submit">Publicar Reporte</button>
            </form>
          </div>
        )}

        {/* MODAL 2: VISTA DETALLADA DE MASCOTA */}
        {mascotaSeleccionada && (
          <div className="form-modal-overlay" onClick={cerrarDetalleMascota}>
            <div className="detalle-mascota-modal" onClick={(e) => e.stopPropagation()}>
              <button className="btn-close" onClick={cerrarDetalleMascota}>✕</button>
              
              {obtenerListaImagenes(mascotaSeleccionada.imagenesUrls).length > 0 ? (
                <div className="detalle-galeria">
                  <div className="detalle-imagen-principal">
                    <img 
                      src={`http://localhost:8081${obtenerListaImagenes(mascotaSeleccionada.imagenesUrls)[imagenActiva]}`} 
                      alt={mascotaSeleccionada.especie}
                    />
                    {obtenerListaImagenes(mascotaSeleccionada.imagenesUrls).length > 1 && (
                      <>
                        <button 
                          className="btn-galeria-prev"
                          onClick={() => setImagenActiva(prev => prev > 0 ? prev - 1 : obtenerListaImagenes(mascotaSeleccionada.imagenesUrls).length - 1)}
                        >
                          ‹
                        </button>
                        <button 
                          className="btn-galeria-next"
                          onClick={() => setImagenActiva(prev => prev < obtenerListaImagenes(mascotaSeleccionada.imagenesUrls).length - 1 ? prev + 1 : 0)}
                        >
                          ›
                        </button>
                        <div className="detalle-contador-imagenes">
                          {imagenActiva + 1} / {obtenerListaImagenes(mascotaSeleccionada.imagenesUrls).length}
                        </div>
                      </>
                    )}
                  </div>
                  
                  {obtenerListaImagenes(mascotaSeleccionada.imagenesUrls).length > 1 && (
                    <div className="detalle-miniaturas">
                      {obtenerListaImagenes(mascotaSeleccionada.imagenesUrls).map((img, index) => (
                        <img 
                          key={index}
                          src={`http://localhost:8081${img}`}
                          alt={`Miniatura ${index + 1}`}
                          className={`miniatura ${index === imagenActiva ? 'activa' : ''}`}
                          onClick={() => setImagenActiva(index)}
                        />
                      ))}
                    </div>
                  )}
                </div>
              ) : (
                <div className="detalle-imagen-principal">
                  <img src={imagenPlaceholder} alt={mascotaSeleccionada.especie} />
                </div>
              )}
              
              <div className="detalle-info">
                <div className="detalle-header">
                  <span className={`detalle-badge status-${mascotaSeleccionada.tipoReporte}`}>
                    {mascotaSeleccionada.tipoReporte}
                  </span>
                  <h2>{mascotaSeleccionada.especie}</h2>
                </div>
                
                <div className="detalle-datos">
                  <div className="detalle-dato">
                    <span className="detalle-label">🦴 Raza:</span>
                    <span className="detalle-valor">{mascotaSeleccionada.raza}</span>
                  </div>
                  <div className="detalle-dato">
                    <span className="detalle-label">📍 Ubicación:</span>
                    <span className="detalle-valor">{mascotaSeleccionada.ubicacion}</span>
                  </div>
                </div>
                
                <div className="detalle-acciones">
                  {mascotaSeleccionada.tipoReporte !== 'REUNIDO' && (
                    <>
                      <button 
                        className="btn-detalle-coincidencias"
                        onClick={() => {
                          cerrarDetalleMascota();
                          buscarCoincidencias(mascotaSeleccionada);
                        }}
                      >
                        🔍 Ver Coincidencias
                      </button>
                      <button 
                        className="btn-detalle-reportar"
                        onClick={() => {
                          cerrarDetalleMascota();
                          setMascotaEncuentro(mascotaSeleccionada);
                        }}
                      >
                        📢 Reportar Encuentro
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* MODAL 3: COINCIDENCIAS */}
        {mostrarModalCoincidencias && mascotaCoincidencias && (
          <div className="form-modal-overlay" onClick={() => setMostrarModalCoincidencias(false)}>
            <div className="coincidencias-modal" onClick={(e) => e.stopPropagation()}>
              <button className="btn-close" onClick={() => setMostrarModalCoincidencias(false)}>✕</button>
              <div className="coincidencias-header">
                <h2>🔍 Coincidencias para {mascotaCoincidencias.especie}</h2>
                <p className="text-muted">Mascotas que podrían ser la misma</p>
              </div>
              <div className="coincidencias-body">
                {cargandoCoincidencias ? (
                  <div className="loading-coincidencias">
                    <div className="spinner"></div>
                    <p>🔍 Calculando coincidencias...</p>
                  </div>
                ) : (
                  <>
                    {obtenerCoincidenciasDeMascota(mascotaCoincidencias.id).length === 0 ? (
                      <div className="no-coincidencias">
                        <p>😔 No se encontraron coincidencias para esta mascota.</p>
                        <p className="text-muted">El sistema analizará automáticamente cuando haya nuevos reportes.</p>
                      </div>
                    ) : (
                      <div className="coincidencias-lista">
                        {obtenerCoincidenciasDeMascota(mascotaCoincidencias.id).map(coincidencia => {
                          const otraMascotaId = coincidencia.idMascotaPerdida === mascotaCoincidencias.id 
                            ? coincidencia.idMascotaEncontrada 
                            : coincidencia.idMascotaPerdida;
                          const otraMascota = obtenerMascotaPorId(otraMascotaId);
                          if (!otraMascota) return null;

                          return (
                            <div key={coincidencia.id} className="coincidencia-card">
                              <div className="coincidencia-imagen">
                                <img src={obtenerUrlImagen(otraMascota.imagenesUrls)} alt={otraMascota.especie} />
                              </div>
                              <div className="coincidencia-info">
                                <h3>{otraMascota.especie}</h3>
                                <p className="coincidencia-raza">{otraMascota.raza}</p>
                                <p className="coincidencia-ubicacion">📍 {otraMascota.ubicacion}</p>
                                <div className="coincidencia-similitud">
                                  <div className="similitud-bar">
                                    <div className="similitud-fill" style={{ width: `${coincidencia.nivelSimilitud}%` }}></div>
                                  </div>
                                  <span className="similitud-texto">{coincidencia.nivelSimilitud}% de similitud</span>
                                </div>
                                <span className={`coincidencia-badge status-${otraMascota.tipoReporte}`}>{otraMascota.tipoReporte}</span>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </>
                )}
              </div>
              <div className="coincidencias-footer">
                <button className="btn-cerrar-coincidencias" onClick={() => setMostrarModalCoincidencias(false)}>Cerrar</button>
              </div>
            </div>
          </div>
        )}

        {/* MODAL 4: REPORTAR ENCUENTRO */}
        {mascotaEncuentro && (
          <div className="form-modal-overlay" onClick={cerrarModalEncuentro}>
            <div className="encuentro-card" onClick={(e) => e.stopPropagation()}>
              <button className="btn-close" onClick={cerrarModalEncuentro}>✕</button>
              <div className="encuentro-header">
                <img src={obtenerUrlImagen(mascotaEncuentro.imagenesUrls)} alt="Mascota" className="encuentro-img" />
                <div className="encuentro-info-header">
                  <span className="badge-encontrada">🎉 ¡MASCOTA ENCONTRADA!</span>
                  <h2>{mascotaEncuentro.especie}</h2>
                  <p>🦴 {mascotaEncuentro.raza} • 📍 {mascotaEncuentro.ubicacion}</p>
                </div>
              </div>
              <div className="encuentro-body">
                <h3>Completa los datos del hallazgo</h3>
                <p className="text-muted">Solo necesitamos algunos datos para verificar el encuentro.</p>
                <div className="upload-section">
                  <div className="upload-header">
                    <label>FOTO DE PRUEBA (OPCIONAL)</label>
                    <span className="upload-counter">{fotoEncuentro ? "1" : "0"} / 1</span>
                  </div>
                  <p className="upload-subtext">Sube una foto actual para verificar que es la misma mascota</p>
                  <label className="upload-box" style={{ padding: fotoEncuentro ? '0' : '20px', overflow: 'hidden', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <input type="file" accept="image/*" style={{ display: 'none' }} onChange={handleImagenChange} />
                    {fotoEncuentro ? (
                      <img src={fotoEncuentro} alt="Vista previa" style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} />
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

        {/* 🗺️ MODAL 5: MAPA DE OPENSTREETMAP */}
        {mostrarMapa && (
          <div className="form-modal-overlay" onClick={() => setMostrarMapa(false)}>
            <div className="mapa-modal" onClick={(e) => e.stopPropagation()}>
              <button className="btn-close" onClick={() => setMostrarMapa(false)}>✕</button>
              
              <div className="mapa-header">
                <h2>📍 Mapa de Mascotas</h2>
                <p>{mascotasParaMapa.length} mascotas registradas</p>
              </div>

              <div className="mapa-container">
                <MapContainer 
                  center={[-33.6119, -70.5746]}
                  zoom={13}
                  style={{ height: '100%', width: '100%' }}
                >
                  <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  />
                  
                  {mascotasParaMapa.map(mascota => {
                    if (!mascota.latitud || !mascota.longitud) return null;
                    
                    const color = mascota.tipoReporte === 'PERDIDA' ? '#ef4444' : 
                                 mascota.tipoReporte === 'ENCONTRADA' ? '#f59e0b' : '#10b981';
                    
                    return (
                      <CircleMarker
                        key={mascota.id}
                        center={[mascota.latitud, mascota.longitud]}
                        radius={12}
                        fillColor={color}
                        color="#fff"
                        weight={2}
                        opacity={1}
                        fillOpacity={0.8}
                      >
                        <Popup>
                          <div className="popup-mascota">
                            <h3>{mascota.especie}</h3>
                            <p><strong>Raza:</strong> {mascota.raza}</p>
                            <p><strong>Estado:</strong> {mascota.tipoReporte}</p>
                            <p><strong>Ubicación:</strong> {mascota.direccionTexto}</p>
                          </div>
                        </Popup>
                      </CircleMarker>
                    );
                  })}
                </MapContainer>
              </div>

              <div className="mapa-leyenda">
                <div className="leyenda-item">
                  <span className="leyenda-color" style={{ background: '#ef4444' }}></span>
                  <span>Perdida</span>
                </div>
                <div className="leyenda-item">
                  <span className="leyenda-color" style={{ background: '#f59e0b' }}></span>
                  <span>Encontrada</span>
                </div>
                <div className="leyenda-item">
                  <span className="leyenda-color" style={{ background: '#10b981' }}></span>
                  <span>Reunido</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* FILTROS PRINCIPALES */}
        <div className="filtros-bar">
          <div className={`filtro-tab ${filtroActivo === 'PERDIDA' ? 'active' : ''}`} onClick={() => setFiltroActivo('PERDIDA')}>🔍 Perdidos</div>
          <div className={`filtro-tab ${filtroActivo === 'ENCONTRADA' ? 'active' : ''}`} onClick={() => setFiltroActivo('ENCONTRADA')}>🧡 Buscan a su Familia</div>
          <div className={`filtro-tab ${filtroActivo === 'REUNIDO' ? 'active' : ''}`} onClick={() => setFiltroActivo('REUNIDO')}>🐾 Reunidos</div>
        </div>

        {/* BARRA DE BÚSQUEDA Y FILTROS */}
        <div className="advanced-filters">
          <div className="search-row">
            <label>Buscar mascotas</label>
            <div className="search-input-wrapper">
              <span>🔍</span>
              <input 
                type="text" 
                placeholder="Nombre, raza, ubicación, descripción..." 
                value={textoBusqueda}
                onChange={(e) => setTextoBusqueda(e.target.value)}
              />
              {textoBusqueda && (
                <button 
                  onClick={() => setTextoBusqueda('')}
                  style={{ 
                    background: 'none', 
                    border: 'none', 
                    cursor: 'pointer', 
                    fontSize: '1.2rem',
                    color: '#94a3b8'
                  }}
                >
                  ✕
                </button>
              )}
            </div>
          </div>
          
          <div className="dropdowns-row">
            <div className="filter-group">
              <label>Especie</label>
              <select value={filtroEspecie} onChange={(e) => setFiltroEspecie(e.target.value)}>
                <option value="TODAS">Todas</option>
                {opcionesEspecie.map(especie => (
                  <option key={especie} value={especie}>{especie}</option>
                ))}
              </select>
            </div>
            <div className="filter-group">
              <label>Raza</label>
              <select value={filtroRaza} onChange={(e) => setFiltroRaza(e.target.value)}>
                <option value="TODAS">Todas</option>
                {opcionesRaza.map(raza => (
                  <option key={raza} value={raza}>{raza}</option>
                ))}
              </select>
            </div>
          </div>

          {(textoBusqueda || filtroEspecie !== 'TODAS' || filtroRaza !== 'TODAS') && (
            <div style={{ marginTop: '15px', textAlign: 'right' }}>
              <button 
                onClick={limpiarFiltros}
                style={{
                  background: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  padding: '8px 16px',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontSize: '0.85rem',
                  color: '#64748b',
                  fontWeight: '600'
                }}
              >
                🧹 Limpiar filtros
              </button>
            </div>
          )}
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
              {mascotasFiltradas.length === 0 && (
                <p style={{ textAlign: 'center', padding: '40px', color: '#64748b', gridColumn: '1 / -1' }}>
                  {textoBusqueda || filtroEspecie !== 'TODAS' || filtroRaza !== 'TODAS'
                    ? '😔 No se encontraron mascotas con esos filtros. Intenta con otra búsqueda.'
                    : 'No hay reportes en esta categoría por ahora.'}
                </p>
              )}
              {mascotasFiltradas.map(m => {
                const esReunido = m.tipoReporte === 'REUNIDO';
                const cantidadImagenes = contarImagenes(m.imagenesUrls);
                const primeraImagen = obtenerUrlImagen(m.imagenesUrls);
                const tieneCoincidencias = obtenerCoincidenciasDeMascota(m.id).length > 0;

                return (
                  <div key={m.id} className={`mascota-card ${esReunido ? 'is-reunido' : ''}`}>
                    <div 
                      className="card-image-container clickable" 
                      onClick={() => abrirDetalleMascota(m)}
                      style={{ cursor: 'pointer' }}
                    >
                      <img src={primeraImagen} alt="Mascota" className="card-image" />
                      {cantidadImagenes > 1 && <span className="gallery-indicator">📷 {cantidadImagenes} fotos</span>}
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
                      <h3 
                        className={`mascota-titulo ${esReunido ? 'reunido-text' : ''}`}
                        onClick={() => abrirDetalleMascota(m)}
                        style={{ cursor: 'pointer' }}
                      >
                        {m.especie}
                      </h3>
                      <p className="mascota-raza">{m.raza}</p>
                      <div className="mascota-ubicacion">📍 <span>{m.ubicacion}</span></div>
                    </div>
                    {!esReunido && (
                      <div className="card-footer">
                        <button className="btn-coincidencias" onClick={() => buscarCoincidencias(m)}>
                          🔍 Ver Coincidencias {tieneCoincidencias && `(${obtenerCoincidenciasDeMascota(m.id).length})`}
                        </button>
                        <button className="btn-reportar" onClick={() => setMascotaEncuentro(m)}>REPORTAR ENCUENTRO</button>
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
            <p className="footer-description">Plataforma dedicada a la búsqueda y rescate de mascotas en la comuna de Puente Alto y sus alrededores.</p>
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
        <Route path="/register" element={<Register />} />
        <Route path="/" element={<ProtectedRoute><MainApp /></ProtectedRoute>} />
      </Routes>
    </Router>
  );
}

export default App;