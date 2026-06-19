import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom'; // 🆕 Agregamos Link

const Login = () => {
    const [correo, setCorreo] = useState('');
    const [contrasena, setContrasena] = useState('');
    const [error, setError] = useState('');
    
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ correo, contrasena })
            });

            if (response.ok) {
                const data = await response.json();
                
                localStorage.setItem('usuarioLogueado', 'true');
                localStorage.setItem('token', data.token);
                localStorage.setItem('correo', data.correo);
                
                navigate('/'); 
            } else {
                setError('Correo o contraseña incorrectos.');
            }
        } catch (err) {
            setError('Error de conexión con el servidor. Revisa si el BFF está encendido.');
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '50px auto', textAlign: 'center' }}>
            <h2>Iniciar Sesión en Sanos y Salvos</h2>
            
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
                
                {/* 🆕 ENLACE AL REGISTRO */}
                <p style={{ marginTop: '20px', color: '#666' }}>
                    ¿No tienes cuenta?{' '}
                    <Link to="/register" style={{ color: '#2196F3', textDecoration: 'none', fontWeight: 'bold' }}>
                        Regístrate aquí
                    </Link>
                </p>
            </form>
        </div>
    );
};

export default Login;