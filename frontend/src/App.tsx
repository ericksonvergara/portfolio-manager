import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AdminLayout } from './features/admin/AdminLayout'
import { AuthProvider } from './features/auth/AuthProvider'
import { ForgotPasswordPage } from './features/auth/ForgotPasswordPage'
import { LoginPage } from './features/auth/LoginPage'
import { ProtectedRoute } from './features/auth/ProtectedRoute'
import { ResetPasswordPage } from './features/auth/ResetPasswordPage'
import { AccountPage } from './pages/admin/AccountPage'
import { AdminProjectsPage } from './pages/admin/AdminProjectsPage'
import { EditProjectPage } from './pages/admin/EditProjectPage'
import { MessagesPage } from './pages/admin/MessagesPage'
import { NewProjectPage } from './pages/admin/NewProjectPage'
import { ProfilePage } from './pages/admin/ProfilePage'
import { TechnologiesPage } from './pages/admin/TechnologiesPage'
import { HomePage } from './pages/HomePage'
import { ProjectDetailPage } from './pages/ProjectDetailPage'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/proyectos/:slug" element={<ProjectDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/recuperar-contrasena"
            element={<ForgotPasswordPage />}
          />
          <Route
            path="/restablecer-contrasena"
            element={<ResetPasswordPage />}
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <AdminLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="proyectos" replace />} />
            <Route path="proyectos" element={<AdminProjectsPage />} />
            <Route path="proyectos/nuevo" element={<NewProjectPage />} />
            <Route path="proyectos/:id/editar" element={<EditProjectPage />} />
            <Route path="tecnologias" element={<TechnologiesPage />} />
            <Route path="perfil" element={<ProfilePage />} />
            <Route path="mensajes" element={<MessagesPage />} />
            <Route path="cuenta" element={<AccountPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
