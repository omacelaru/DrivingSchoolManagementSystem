import { Component, type ErrorInfo, type ReactNode } from "react";

type Props = { children: ReactNode };
type State = { hasError: boolean };

export class ErrorBoundary extends Component<Props, State> {
  public state: State = { hasError: false };

  public static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error("Unhandled UI error", error, errorInfo);
  }

  public render(): ReactNode {
    if (this.state.hasError) {
      return (
        <div className="auth-page">
          <div className="centered-page">
            <div className="card">
              <h1>500 — Unexpected error</h1>
              <p>The application encountered an unexpected error. Please reload the page.</p>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
