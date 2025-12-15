import { InstagramConnection } from '@/components/instagram/InstagramConnection';

export const SettingsPage = () => {
  return (
    <div className="container py-8">
      <h1 className="text-3xl font-bold mb-8">Innstillinger</h1>

      <div className="space-y-6">
        <section>
          <h2 className="text-xl font-semibold mb-4">Sosiale medier</h2>
          <InstagramConnection />
        </section>
      </div>
    </div>
  );
};
