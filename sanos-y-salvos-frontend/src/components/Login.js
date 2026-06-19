import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // Usamos esto para cambiar de página al ingresar

const Login = () => {
    // Estados para guardar lo que el usuario escribe
    const [correo, setCorreo] = useState('');
    const [contrasena, setContrasena] = useState('');
    const [error, setError] = useState('');
    
    const navigate = useNavigate();

    // Función que se ejecuta al apretar el botón "Ingresar"
    const handleSubmit = async (e) => {
        e.preventDefault(); // Evita que la página se recargue en blanco
        setError(''); // Limpiamos errores previos

        try {
            // El fetch va directo a tu BFF en el puerto 8080
            const response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ correo, contrasena })
            });

            if (response.ok) {
                const data = await response.json();
                
                // ¡Éxito! Guardamos el "pase VIP" en la memoria del navegador
                localStorage.setItem('usuarioLogueado', 'true');
                localStorage.setItem('token', data.token);
                localStorage.setItem('correo', data.correo);
                
                // Redirigimos al usuario a la página principal de reportes de mascotas
                navigate('/'); 
            } else {
                // Si el BFF devuelve un error 401
                setError('Correo o contraseña incorrectos.');
            }
        } catch (err) {
            setError('Error de conexión con el servidor. Revisa si el BFF está encendido.');
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '50px auto', textAlign: 'center' }}>
            <h2>Iniciar Sesión en Sanos y Salvos</h2>
            
            {/* Si hay un error, lo mostramos en rojo */}
            {error && <p style={{ color: 'red', fontWeight: 'bold' }}>{error}</p>}
            
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                <div>
                    <label>Correo Electrónico: </label>
                    <input 
                        type="email" 
                        value={correo} 
                        onChange={(e) => setCorreo(e.target.value)} 
                        required 
                        style={{ width: '100%', padding: '8px' }}
                    />
                </div>
                <div>
                    <label>Contraseña: </label>
                    <input 
                        type="password" 
                        value={contrasena} 
                        onChange={(e) => setContrasena(e.target.value)} 
                        required 
                        style={{ width: '100%', padding: '8px' }}
                    />
                </div>
                <button type="submit" style={{ padding: '10px', backgroundColor: '#4CAF50', color: 'white', border: 'none', cursor: 'pointer' }}>
                    Ingresar
                </button>
            </form>
        </div>
    );
};

export default Login;