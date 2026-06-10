import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [mascotas, setMascotas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [formulario, setFormulario] = useState({
    tipoReporte: 'PERDIDA',
    especie: '',
    raza: '',
    ubicacion: ''
  });

  // 1. Función para obtener las mascotas (ahora con estado de carga)
  const cargarMascotas = async () => {
    try {
      setLoading(true);
      const res = await axios.get('/api/mascotas');
      setMascotas(res.data);
    } catch (error) {
      console.error("Error al conectar con el backend:", error);
      alert("No se pudo conectar con el servidor. Revisa si el Backend y el BFF están encendidos.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarMascotas();
  }, []);

  // 2. Manejar cambios en los inputs
  const handleChange = (e) => {
    setFormulario({ 
      ...formulario, 
      [e.target.name]: e.target.value 
    });
  };

  // 3. Enviar datos y limpiar el formulario
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('/api/mascotas', formulario);
      alert("✅ ¡Mascota registrada exitosamente!");
      
      // Limpiamos los campos para el siguiente registro
      setFormulario({
        tipoReporte: 'PERDIDA',
        especie: '',
        raza: '',
        ubicacion: ''
      });

      cargarMascotas(); // Refrescar la lista automáticamente
    } catch (error) {
      console.error("Error al guardar:", error);
      alert("❌ Error al guardar la mascota.");
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Sanos y Salvos 🐾</h1>
        <p>Microservicios de Recuperación de Mascotas</p>
      </header>

      <main className="container">
        {/* Formulario de registro */}
        <form onSubmit={handleSubmit} className="form-card">
          <h2>Registrar Reporte</h2>
          <label>Estado:</label>
          <select name="tipoReporte" value={formulario.tipoReporte} onChange={handleChange}>
            <option value="PERDIDA">Perdida</option>
            <option value="ENCONTRADA">Encontrada</option>
          </select>
          
          <input 
            name="especie" 
            placeholder="Especie (Ej: Perro)" 
            value={formulario.especie} 
            onChange={handleChange} 
            required 
          />
          <input 
            name="raza" 
            placeholder="Raza" 
            value={formulario.raza} 
            onChange={handleChange} 
            required 
          />
          <input 
            name="ubicacion" 
            placeholder="Ubicación" 
            value={formulario.ubicacion} 
            onChange={handleChange} 
            required 
          />
          
          <button type="submit">Publicar Reporte</button>
        </form>

        {/* Listado de reportes */}
        <section className="listado">
          <h2>Reportes Recientes</h2>
          {loading ? (
            <p>Cargando mascotas...</p>
          ) : (
            <div className="grid">
              {mascotas.length === 0 && <p>No hay reportes registrados aún.</p>}
              {mascotas.map(m => (
                <div key={m.id} className="mascota-item">
                  <span className={`badge ${m.tipoReporte}`}>{m.tipoReporte}</span>
                  <h3>{m.especie} - {m.raza}</h3>
                  <p>📍 {m.ubicacion}</p>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;