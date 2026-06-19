import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const Register = () => {
    const [correo, setCorreo] = useState('');
    const [contrasena, setContrasena] = useState('');
    const [confirmarContrasena, setConfirmarContrasena] = useState('');
    const [error, setError] = useState('');
    const [exito, setExito] = useState('');
    
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setExito('');

        // Validar que las contraseñas coincidan
        if (contrasena !== confirmarContrasena) {
            setError('Las contraseñas no coinciden');
            return;
        }

        // Validar longitud mínima
        if (contrasena.length < 6) {
            setError('La contraseña debe tener al menos 6 caracteres');
            return;
        }

        try {
            // El fetch va al BFF en el puerto 8080
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
                
                // Después de 2 segundos, redirigir al login
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            } else {
                const errorData = await response.json();
                setError(errorData.error || 'Error al registrarse. El correo podría estar en uso.');
            }
        } catch (err) {
            setError('Error de conexión con el servidor. Revisa si el BFF está encendido.');
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '50px auto', textAlign: 'center' }}>
            <h2>Crear Cuenta en Sanos y Salvos</h2>
            
            {error && <p style={{ color: 'red', fontWeight: 'bold' }}>{error}</p>}
            {exito && <p style={{ color: 'green', fontWeight: 'bold' }}>{exito}</p>}
            
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
                        placeholder="Mínimo 6 caracteres"
                    />
                </div>
                <div>
                    <label>Confirmar Contraseña: </label>
                    <input 
                        type="password" 
                        value={confirmarContrasena} 
                        onChange={(e) => setConfirmarContrasena(e.target.value)} 
                        required 
                        style={{ width: '100%', padding: '8px' }}
                    />
                </div>
                <button type="submit" style={{ padding: '10px', backgroundColor: '#2196F3', color: 'white', border: 'none', cursor: 'pointer' }}>
                    Crear Cuenta
                </button>
                <button 
                    type="button" 
                    onClick={() => navigate('/login')}
                    style={{ padding: '10px', backgroundColor: '#f0f0f0', color: '#333', border: '1px solid #ddd', cursor: 'pointer' }}
                >
                    Ya tengo cuenta
                </button>
            </form>
        </div>
    );
};

export default Register;