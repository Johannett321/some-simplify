import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { InstagramApi, type SocialMediaConnectionTO } from '@/api';
import { apiConfig } from '@/config/ApiConfig';

export function InstagramConnection() {
  const [connection, setConnection] = useState<SocialMediaConnectionTO | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadConnection();
  }, []);

  const loadConnection = async () => {
    try {
      const api = new InstagramApi(apiConfig);
      const response = await api.getInstagramConnection();
      setConnection(response.data);
    } catch (error) {
      setConnection(null);
    }
  };

  const handleConnect = async () => {
    setLoading(true);
    try {
      const api = new InstagramApi(apiConfig);
      const response = await api.getInstagramAuthUrl();

      window.location.href = response.data.authUrl!;
    } catch (error) {
      console.error('Failed to get auth URL:', error);
      setLoading(false);
    }
  };

  const handleDisconnect = async () => {
    if (!confirm('Er du sikker på at du vil koble fra Instagram?')) return;

    setLoading(true);
    try {
      const api = new InstagramApi(apiConfig);
      await api.disconnectInstagram();
      setConnection(null);
    } catch (error) {
      console.error('Failed to disconnect:', error);
    }
    setLoading(false);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Instagram</CardTitle>
        <CardDescription>
          Koble til Instagram for automatisk publisering
        </CardDescription>
      </CardHeader>
      <CardContent>
        {connection ? (
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 rounded-full bg-green-500" />
              <span>Tilkoblet som @{connection.accountName}</span>
            </div>

            {connection.lastPublishedAt && (
              <p className="text-sm text-muted-foreground">
                Sist publisert: {new Date(connection.lastPublishedAt).toLocaleString('nb-NO')}
              </p>
            )}

            {connection.lastError && (
              <p className="text-sm text-destructive">
                Siste feil: {connection.lastError}
              </p>
            )}

            <Button
              variant="destructive"
              onClick={handleDisconnect}
              disabled={loading}
            >
              Koble fra
            </Button>
          </div>
        ) : (
          <Button onClick={handleConnect} disabled={loading}>
            Koble til Instagram
          </Button>
        )}
      </CardContent>
    </Card>
  );
}
