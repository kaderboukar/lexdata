import React, { Suspense } from "react";
import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import PageLoader from "../common/PageLoader";

export default function PublicLayout() {
  return (
    <div className="page-wrapper fade-in">
      <Navbar />
      <main className="main-content">
        <Suspense fallback={<PageLoader />}>
          <Outlet />
        </Suspense>
      </main>
    </div>
  );
}

