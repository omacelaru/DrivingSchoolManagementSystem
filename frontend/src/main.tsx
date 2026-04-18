import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { appRouter } from "./router";
import { ThemeProvider } from "./theme/ThemeContext";
import { applyDocumentTheme, getPreferredTheme } from "./theme/storage";
import "./styles.css";

applyDocumentTheme(getPreferredTheme());

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <ThemeProvider>
      <RouterProvider router={appRouter} />
    </ThemeProvider>
  </React.StrictMode>
);
