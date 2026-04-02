import React, { Suspense } from "react";
import { Outlet } from "react-router-dom";
import PageLoader from "../common/PageLoader";

function Sidebar() {
  return <aside className="glass-card portal-sidebar">Sidebar</aside>;
}

function TopBar() {
  return <header className="glass-card portal-topbar">TopBar</header>;
}

export default function PortalLayout() {
  return (
    <div className="page-wrapper fade-in">
      <div className="container portal-shell">
        <Sidebar />
        <div className="portal-main">
          <TopBar />
          <main className="portal-content">
            <Suspense fallback={<PageLoader />}>
              <Outlet />
            </Suspense>
          </main>
        </div>
      </div>
    </div>
  );
}

