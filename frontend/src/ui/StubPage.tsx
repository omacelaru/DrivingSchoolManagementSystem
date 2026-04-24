type Props = { title: string };

export function StubPage({ title }: Props): JSX.Element {
  return (
    <section className="page">
      <h1>{title}</h1>
      <p>This module is prepared for CRUD integration in the next step.</p>
    </section>
  );
}
