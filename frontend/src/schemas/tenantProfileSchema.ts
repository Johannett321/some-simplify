import { z } from 'zod';

// Predefined options
export const CONCEPT_OPTIONS = [
  'Bakeri',
  'Brun Pub',
  'Fine Dining',
  'Nattklubb',
  'Annet'
] as const;

export const TARGET_AUDIENCE_OPTIONS = [
  'Studenter',
  'Barnefamilier',
  'Business',
  'Turister',
  'Annet'
] as const;

export const tenantProfileSchema = z.object({
  address: z.string().min(1, 'Adresse er påkrevd'),
  concept: z.enum(CONCEPT_OPTIONS, {
    required_error: 'Konsept er påkrevd',
  }),
  conceptOther: z.string().optional(),
  targetAudience: z.array(z.string()).min(1, 'Velg minst én målgruppe'),
  targetAudienceOther: z.string().optional(),
  websiteUrl: z.string().url('Ugyldig URL').min(1, 'Nettside er påkrevd'),
}).refine(
  (data) => {
    // If concept is "Annet", conceptOther must be provided
    if (data.concept === 'Annet') {
      return data.conceptOther && data.conceptOther.trim().length > 0;
    }
    return true;
  },
  {
    message: 'Vennligst spesifiser konsept',
    path: ['conceptOther'],
  }
).refine(
  (data) => {
    // If targetAudience includes "Annet", targetAudienceOther must be provided
    if (data.targetAudience.includes('Annet')) {
      return data.targetAudienceOther && data.targetAudienceOther.trim().length > 0;
    }
    return true;
  },
  {
    message: 'Vennligst spesifiser målgruppe',
    path: ['targetAudienceOther'],
  }
);

export type TenantProfileFormData = z.infer<typeof tenantProfileSchema>;
