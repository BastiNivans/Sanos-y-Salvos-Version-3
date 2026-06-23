import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Auth.css';

const Register = () => {
    const [correo, setCorreo] = useState('');
    const [contrasena, setContrasena] = useState('');
    const [confirmarContrasena, setConfirmarContrasena] = useState('');
    const [error, setError] = useState('');
    const [exito, setExito] = useState('');
    const [cargando, setCargando] = useState(false);
    
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setExito('');

        if (contrasena !== confirmarContrasena) {
            setError('Las contraseñas no coinciden');
            return;
        }

        if (contrasena.length < 6) {
            setError('La contraseña debe tener al menos 6 caracteres');
            return;
        }

        setCargando(true);

        try {
            const response = await fetch('http://localhost:8080/api/registro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ correo, contrasena })
            });

            if (response.ok) {
                const data = await response.json();
                setExito('✅ ¡Registro exitoso! Redirigiendo al login...');
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            } else {
                const errorData = await response.json();
                setError(errorData.error || 'Error al registrarse. El correo podría estar en uso.');
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
                    <h2>Crear Cuenta</h2>
                    
                    {error && <div className="error-message">{error}</div>}
                    {exito && <div className="success-message">{exito}</div>}

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
                            placeholder="Mínimo 6 caracteres"
                            required
                        />
                        <p className="password-requirements">Debe tener al menos 6 caracteres</p>
                    </div>

                    <div className="form-group">
                        <label>Confirmar Contraseña</label>
                        <input
                            type="password"
                            value={confirmarContrasena}
                            onChange={(e) => setConfirmarContrasena(e.target.value)}
                            placeholder="Repite tu contraseña"
                            required
                        />
                    </div>

                    <button type="submit" className="btn-auth" disabled={cargando}>
                        {cargando ? 'CREANDO CUENTA...' : 'CREAR CUENTA'}
                    </button>

                    <button 
                        type="button" 
                        onClick={() => navigate('/login')}
                        className="btn-secondary"
                    >
                        Ya tengo cuenta
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Register;