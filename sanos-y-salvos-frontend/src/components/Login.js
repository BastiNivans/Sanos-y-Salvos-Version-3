import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Auth.css';

const Login = () => {
    const [correo, setCorreo] = useState('');
    const [contrasena, setContrasena] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);
    
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setCargando(true);

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
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <span className="auth-logo">🐾</span>
                    <h1>SANOS Y SALVOS</h1>
                    <p>Red de Rescate de Mascotas</p>
                </div>

                <form onSubmit={handleSubmit} className="auth-form">
                    <h2>Iniciar Sesión</h2>
                    
                    {error && <div className="error-message">{error}</div>}

                    <div className="form-group">
                        <label>Correo Electrónico</label>
                        <input
                            type="email"
                            value={correo}
                            onChange={(e) => setCorreo(e.target.value)}
                            placeholder="tucorreo@ejemplo.com"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Contraseña</label>
                        <input
                            type="password"
                            value={contrasena}
                            onChange={(e) => setContrasena(e.target.value)}
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <button type="submit" className="btn-auth" disabled={cargando}>
                        {cargando ? 'INGRESANDO...' : 'INGRESAR'}
                    </button>

                    <div className="auth-footer">
                        <p>¿No tienes cuenta? <Link to="/register">Regístrate aquí</Link></p>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Login;