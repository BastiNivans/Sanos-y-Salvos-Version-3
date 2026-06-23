import React from 'react';
import { useNavigate } from 'react-router-dom';

function Contacto() {
  const navigate = useNavigate();

  return (
    <div style={{ 
      minHeight: '100vh',
      background: '#f8fafc'
    }}>
      {/* Header similar al de la app */}
      <header style={{ 
        background: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)',
        color: 'white',
        padding: '30px 20px',
        textAlign: 'center',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
      }}>
        <h1 style={{ margin: 0, fontSize: '2.5rem', fontWeight: 'bold' }}>
          📞 Contacto
        </h1>
        <p style={{ margin: '10px 0 0 0', fontSize: '1.1rem', opacity: 0.9 }}>
          Estamos aquí para ayudarte
        </p>
      </header>

      <div style={{ 
        padding: '40px 20px', 
        maxWidth: '1000px', 
        margin: '0 auto'
      }}>
        {/* Grid de información de contacto */}
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
          gap: '25px',
          marginBottom: '30px'
        }}>
          {/* Email */}
          <div style={{
            background: 'white',
            padding: '30px',
            borderRadius: '12px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            transition: 'transform 0.3s ease, box-shadow 0.3s ease',
            cursor: 'pointer',
            textAlign: 'center'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-5px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
          }}
          onClick={() => window.location.href = 'mailto:contacto@sanosysalvos.cl'}
          >
            <div style={{ 
              fontSize: '3rem', 
              marginBottom: '15px' 
            }}>
              ✉️
            </div>
            <h3 style={{ 
              color: '#1e293b', 
              marginBottom: '10px',
              fontSize: '1.3rem'
            }}>
              Email
            </h3>
            <p style={{ 
              color: '#3b82f6', 
              fontSize: '1.1rem',
              fontWeight: '600',
              margin: 0
            }}>
              contacto@sanosysalvos.cl
            </p>
            <p style={{ 
              color: '#64748b', 
              fontSize: '0.9rem',
              marginTop: '8px'
            }}>
              Te responderemos en menos de 24 horas
            </p>
          </div>

          {/* Teléfono / WhatsApp */}
          <div style={{
            background: 'white',
            padding: '30px',
            borderRadius: '12px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            transition: 'transform 0.3s ease, box-shadow 0.3s ease',
            cursor: 'pointer',
            textAlign: 'center'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-5px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
          }}
          onClick={() => window.open('https://wa.me/56912345678', '_blank')}
          >
            <div style={{ 
              fontSize: '3rem', 
              marginBottom: '15px' 
            }}>
              📱
            </div>
            <h3 style={{ 
              color: '#1e293b', 
              marginBottom: '10px',
              fontSize: '1.3rem'
            }}>
              Teléfono / WhatsApp
            </h3>
            <p style={{ 
              color: '#3b82f6', 
              fontSize: '1.1rem',
              fontWeight: '600',
              margin: 0
            }}>
              +56 9 1234 5678
            </p>
            <p style={{ 
              color: '#64748b', 
              fontSize: '0.9rem',
              marginTop: '8px'
            }}>
              Disponible de Lunes a Sábado
            </p>
          </div>

          {/* Ubicación */}
          <div style={{
            background: 'white',
            padding: '30px',
            borderRadius: '12px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            transition: 'transform 0.3s ease, box-shadow 0.3s ease',
            cursor: 'pointer',
            textAlign: 'center'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-5px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
          }}
          onClick={() => window.open('https://maps.google.com/?q=Av.+Concha+y+Toro+1234,Puente+Alto', '_blank')}
          >
            <div style={{ 
              fontSize: '3rem', 
              marginBottom: '15px' 
            }}>
              📍
            </div>
            <h3 style={{ 
              color: '#1e293b', 
              marginBottom: '10px',
              fontSize: '1.3rem'
            }}>
              Ubicación
            </h3>
            <p style={{ 
              color: '#475569', 
              fontSize: '1rem',
              margin: '0 0 10px 0',
              lineHeight: '1.5'
            }}>
              Av. Concha y Toro 1234<br/>
              Puente Alto, Santiago
            </p>
            <span style={{
              display: 'inline-block',
              background: '#3b82f6',
              color: 'white',
              padding: '6px 16px',
              borderRadius: '6px',
              fontSize: '0.9rem',
              fontWeight: '600',
              marginTop: '10px'
            }}>
              🗺️ Ver en Maps
            </span>
          </div>
        </div>

        {/* Horario de Atención - Card más grande */}
        <div style={{
          background: 'white',
          padding: '30px',
          borderRadius: '12px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          marginBottom: '30px',
          textAlign: 'center'
        }}>
          <div style={{ fontSize: '3rem', marginBottom: '15px' }}>
            🕐
          </div>
          <h3 style={{ 
            color: '#1e293b', 
            marginBottom: '20px',
            fontSize: '1.5rem'
          }}>
            Horario de Atención
          </h3>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
            gap: '20px',
            maxWidth: '600px',
            margin: '0 auto'
          }}>
            <div style={{
              background: '#f1f5f9',
              padding: '20px',
              borderRadius: '8px',
              borderLeft: '4px solid #3b82f6'
            }}>
              <p style={{ 
                color: '#1e293b', 
                fontWeight: '700',
                margin: '0 0 5px 0',
                fontSize: '1.1rem'
              }}>
                Lunes a Viernes
              </p>
              <p style={{ 
                color: '#64748b', 
                margin: 0,
                fontSize: '1rem'
              }}>
                9:00 - 18:00 hrs
              </p>
            </div>
            <div style={{
              background: '#f1f5f9',
              padding: '20px',
              borderRadius: '8px',
              borderLeft: '4px solid #10b981'
            }}>
              <p style={{ 
                color: '#1e293b', 
                fontWeight: '700',
                margin: '0 0 5px 0',
                fontSize: '1.1rem'
              }}>
                Sábados
              </p>
              <p style={{ 
                color: '#64748b', 
                margin: 0,
                fontSize: '1rem'
              }}>
                10:00 - 14:00 hrs
              </p>
            </div>
          </div>
        </div>

        {/* Mapa embebido (opcional) */}
        <div style={{
          background: 'white',
          borderRadius: '12px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          overflow: 'hidden',
          marginBottom: '30px'
        }}>
          <div style={{ padding: '20px', background: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
            <h3 style={{ margin: 0, color: '#1e293b', fontSize: '1.3rem' }}>
              📍 Nuestra Ubicación
            </h3>
          </div>
          <iframe
            src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3325.1234567890123!2d-70.5746!3d-33.6119!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2zMzPCsDM2JzQyLjgiUyA3MMKwMzQnMjguNiJX!5e0!3m2!1ses!2scl!4v1234567890123!5m2!1ses!2scl"
            width="100%"
            height="350"
            style={{ border: 0 }}
            allowFullScreen=""
            loading="lazy"
            referrerPolicy="no-referrer-when-downgrade"
            title="Ubicación Sanos y Salvos"
          ></iframe>
        </div>

        {/* Botón Volver */}
        <div style={{ textAlign: 'center' }}>
          <button
            onClick={() => navigate('/')}
            style={{
              background: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)',
              color: 'white',
              border: 'none',
              padding: '15px 40px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '1.1rem',
              fontWeight: '600',
              boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
              transition: 'transform 0.2s ease, box-shadow 0.2s ease'
            }}
            onMouseEnter={(e) => {
              e.target.style.transform = 'translateY(-2px)';
              e.target.style.boxShadow = '0 6px 12px rgba(0,0,0,0.15)';
            }}
            onMouseLeave={(e) => {
              e.target.style.transform = 'translateY(0)';
              e.target.style.boxShadow = '0 4px 6px rgba(0,0,0,0.1)';
            }}
          >
            ← Volver al Inicio
          </button>
        </div>
      </div>
    </div>
  );
}

export default Contacto;