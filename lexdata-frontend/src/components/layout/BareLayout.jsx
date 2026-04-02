import React, { Suspense } from "react";
import { Outlet } from "react-router-dom";
import PageLoader from "../common/PageLoader";

export default function BareLayout() {
  return (
    <div className="page-wrapper fade-in">
      <Suspense fallback={<PageLoader />}>
        <Outlet />
      </Suspense>
    </div>
  );
}

